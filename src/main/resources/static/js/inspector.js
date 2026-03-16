/* =====================================================
   SkyLock — File Inspector Panel
   ===================================================== */

(function () {
    'use strict';

    const inspectorPanel = document.getElementById('inspector-panel');

    // ── Select File ──────────────────────────────────
    skylock.selectFile = function (file, card) {
        document.querySelectorAll('.asset-card.selected')
            .forEach(c => c.classList.remove('selected'));
        card.classList.add('selected');

        skylock.state.selectedFileId = file.id;
        populateInspector(file);
        openInspector();
    };

    function populateInspector(file) {
        const previewIcon = getPreviewIcon(file);
        document.getElementById('inspector-preview').innerHTML = previewIcon;
        document.getElementById('inspector-filename').textContent = file.filename;
        document.getElementById('inspector-type').textContent =
            (file.type || 'File').toUpperCase() + ' File';
        document.getElementById('inspector-size').textContent =
            skylock.formatSize(file.size);
        document.getElementById('inspector-created').textContent =
            skylock.formatDate(file.uploadedAt);

        // Download link
        const dlBtn = document.getElementById('btn-download');
        dlBtn.onclick = () => {
            skylock.downloadFile(file);
        };

        // Preview link
        const previewBtn = document.getElementById('btn-preview');
        if (previewBtn) {
            previewBtn.onclick = () => {
                skylock.previewFile(file);
            };
        }

        // Share button
        const shareBtn = document.getElementById('btn-share-inspector');
        if (shareBtn) {
            shareBtn.onclick = () => {
                skylock.openShareModal('file', file.id, file.filename);
            };
        }

        // Tags
        const tagRow = document.getElementById('inspector-tags');
        tagRow.innerHTML = '';
        const type = file.type || 'file';
        const typeTag = document.createElement('span');
        typeTag.className = 'tag ' + type;
        typeTag.textContent = type.toUpperCase();
        tagRow.appendChild(typeTag);
    }

    function getPreviewIcon(file) {
        if (file.type === 'image') {
            return `<span class="neon-icon">🖼️</span>`;
        }
        const icons = {
            pdf: '📄', document: '📝', spreadsheet: '📊',
            video: '🎬', audio: '🎵', archive: '📦', file: '📎'
        };
        return `<span class="neon-icon">${icons[file.type] || '📎'}</span>`;
    }

    // ── Inspector Open / Close ───────────────────────
    function openInspector() {
        inspectorPanel.classList.add('open');
    }

    skylock.closeInspector = function () {
        inspectorPanel.classList.remove('open');
        skylock.state.selectedFileId = null;
        document.querySelectorAll('.asset-card.selected')
            .forEach(c => c.classList.remove('selected'));
    };

})();
