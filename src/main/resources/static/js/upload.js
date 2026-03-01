/* =====================================================
   SkyLock — Chunked Upload Module
   ===================================================== */

(function () {
    'use strict';

    // ── Config ───────────────────────────────────────
    const CHUNK_SIZE = 5 * 1024 * 1024;   // 5 MB per chunk
    const MAX_PARALLEL_UPLOADS = 6;

    let uploadCancelled = false;
    let activeXHRs = [];

    const uploadModal = document.getElementById('upload-modal');

    // ── Bind Upload Modal ────────────────────────────
    skylock.bindUploadModal = function () {

        const uploadBtn = document.getElementById('upload-btn');
        const closeBtn = document.getElementById('close-upload');
        const cancelBtn = document.getElementById('cancel-upload');
        const confirmBtn = document.getElementById('confirm-upload');
        const dropZone = document.getElementById('drop-zone');
        const fileInput = document.getElementById('file-input');
        const selectedFileName = document.getElementById('selected-file-name');

        uploadBtn?.addEventListener("click",
            () => uploadModal.classList.add("visible"));

        closeBtn?.addEventListener("click", closeUploadModal);
        cancelBtn?.addEventListener("click", closeUploadModal);

        // Drag & Drop
        dropZone?.addEventListener("click", () => fileInput.click());

        dropZone?.addEventListener("dragover", e => {
            e.preventDefault();
            dropZone.classList.add("dragover");
        });

        dropZone?.addEventListener("dragleave",
            () => dropZone.classList.remove("dragover"));

        dropZone?.addEventListener("drop", e => {
            e.preventDefault();
            dropZone.classList.remove("dragover");
            fileInput.files = e.dataTransfer.files;
            showSelectedFile(e.dataTransfer.files[0].name);
        });

        fileInput?.addEventListener("change", () => {
            if (fileInput.files.length)
                showSelectedFile(fileInput.files[0].name);
        });

        confirmBtn?.addEventListener("click", async () => {
            if (!fileInput.files.length) {
                skylock.showToast("Select file first", "error");
                return;
            }
            await uploadFile(fileInput.files[0]);
        });

        function showSelectedFile(name) {
            selectedFileName.textContent = "📎 " + name;
            selectedFileName.style.display = "block";
        }
    };

    // ── Chunked Upload Flow ──────────────────────────
    async function uploadFile(file) {

        uploadCancelled = false;
        activeXHRs = [];

        const progressBar = document.getElementById("upload-progress");
        const progressFill = document.getElementById("progress-fill");
        const progressPercent = document.getElementById("progress-percent");
        const progressSize = document.getElementById("progress-size");
        const confirmBtn = document.getElementById("confirm-upload");

        // Show progress bar
        if (progressBar) progressBar.classList.add("active");
        if (progressFill) progressFill.style.width = "0%";
        if (progressPercent) progressPercent.textContent = "0%";
        if (progressSize) progressSize.textContent =
            skylock.formatSize(0) + " / " + skylock.formatSize(file.size);

        confirmBtn.disabled = true;
        confirmBtn.textContent = "Uploading...";

        const totalChunks = Math.ceil(file.size / CHUNK_SIZE);

        let uploadedBytes = 0;
        let active = 0;
        let index = 0;

        function updateProgress(delta) {
            uploadedBytes += delta;
            const pct = Math.round((uploadedBytes / file.size) * 95);
            progressFill.style.width = pct + "%";
            progressPercent.textContent = pct + "%";
            progressSize.textContent =
                skylock.formatSize(uploadedBytes) + " / " + skylock.formatSize(file.size);
        }

        return new Promise((resolve, reject) => {

            function startWorker() {
                if (uploadCancelled) return reject("Cancelled");

                while (active < MAX_PARALLEL_UPLOADS && index < totalChunks) {
                    const chunkNumber = index++;
                    active++;

                    const start = chunkNumber * CHUNK_SIZE;
                    const end = Math.min(start + CHUNK_SIZE, file.size);
                    const blob = file.slice(start, end);

                    sendChunk(blob, file.name, chunkNumber, totalChunks, updateProgress)
                        .then(() => {
                            active--;
                            startWorker();
                        })
                        .catch(reject);
                }

                if (index >= totalChunks && active === 0) {
                    mergePhase();
                }
            }

            async function mergePhase() {
                confirmBtn.textContent = "Merging...";

                await mergeFile(file.name, totalChunks);

                progressFill.style.width = "100%";
                progressPercent.textContent = "100%";

                skylock.showToast("File uploaded successfully!", "success");

                setTimeout(() => {
                    closeUploadModal();
                    skylock.loadDashboard();
                }, 400);

                resolve();
            }

            startWorker();
        });
    }

    // ── Send Single Chunk ────────────────────────────
    function sendChunk(chunkBlob, fileName, chunkNumber, totalChunks, onProgress) {
        return new Promise((resolve, reject) => {
            const formData = new FormData();
            formData.append("chunk", chunkBlob);
            formData.append("fileName", fileName);
            formData.append("chunkNumber", chunkNumber);
            formData.append("totalChunks", totalChunks);

            if (skylock.state.currentFolderId)
                formData.append("folderId", skylock.state.currentFolderId);

            const xhr = new XMLHttpRequest();
            activeXHRs.push(xhr);

            xhr.upload.onprogress = e => {
                if (e.lengthComputable) {
                    const delta = e.loaded - (xhr._last || 0);
                    xhr._last = e.loaded;
                    onProgress(delta);
                }
            };

            xhr.onload = () => {
                removeXHR(xhr);
                (xhr.status >= 200 && xhr.status < 300)
                    ? resolve()
                    : reject(xhr.responseText);
            };

            xhr.onerror = () => reject("Network error");

            xhr.open("POST", "/upload-chunk");
            xhr.send(formData);
        });
    }

    // ── Merge Request ────────────────────────────────
    function mergeFile(fileName, totalChunks) {
        return new Promise((resolve, reject) => {
            const fd = new FormData();
            fd.append("fileName", fileName);
            fd.append("totalChunks", totalChunks);

            if (skylock.state.currentFolderId)
                fd.append("folderId", skylock.state.currentFolderId);

            const xhr = new XMLHttpRequest();

            xhr.onload = () => {
                (xhr.status >= 200 && xhr.status < 300)
                    ? resolve()
                    : reject("Merge failed");
            };

            xhr.open("POST", "/merge-file");
            xhr.send(fd);
        });
    }

    // ── Close & Reset ────────────────────────────────
    function closeUploadModal() {
        uploadCancelled = true;
        activeXHRs.forEach(x => x.abort());
        activeXHRs = [];

        uploadModal.classList.remove("visible");

        const fileInput = document.getElementById("file-input");
        const selectedFileName = document.getElementById("selected-file-name");
        if (fileInput) fileInput.value = "";
        if (selectedFileName) selectedFileName.style.display = "none";

        resetUploadUI();
    }

    function resetUploadUI() {
        const progressBar = document.getElementById("upload-progress");
        const progressFill = document.getElementById("progress-fill");
        const progressPercent = document.getElementById("progress-percent");
        const progressSize = document.getElementById("progress-size");
        const confirmBtn = document.getElementById("confirm-upload");

        if (progressBar) progressBar.classList.remove("active");
        if (progressFill) progressFill.style.width = "0";
        if (progressPercent) progressPercent.textContent = "0%";
        if (progressSize) progressSize.textContent = "";
        if (confirmBtn) {
            confirmBtn.disabled = false;
            confirmBtn.textContent = "Upload";
        }
    }

    function removeXHR(xhr) {
        activeXHRs = activeXHRs.filter(x => x !== xhr);
    }

})();
