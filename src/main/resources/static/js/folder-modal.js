/* =====================================================
   SkyLock — Folder Modal
   ===================================================== */

(function () {
    'use strict';

    const folderModal = document.getElementById('folder-modal');

    skylock.bindFolderModal = function () {
        const folderBtn = document.getElementById('new-folder-btn');
        const cancelBtn = document.getElementById('cancel-folder');
        const confirmBtn = document.getElementById('confirm-folder');
        const nameInput = document.getElementById('folder-name-input');

        if (folderBtn) {
            folderBtn.addEventListener('click', () => {
                if (folderModal) {
                    folderModal.classList.add('visible');
                    if (nameInput) {
                        nameInput.value = '';
                        nameInput.focus();
                    }
                }
            });
        }

        if (cancelBtn) cancelBtn.addEventListener('click', closeFolderModal);

        if (folderModal) {
            folderModal.addEventListener('click', (e) => {
                if (e.target === folderModal) closeFolderModal();
            });
        }

        if (nameInput) {
            nameInput.addEventListener('keydown', (e) => {
                if (e.key === 'Enter') createNewFolder();
            });
        }

        if (confirmBtn) {
            confirmBtn.addEventListener('click', createNewFolder);
        }
    };

    async function createNewFolder() {
        const nameInput = document.getElementById('folder-name-input');
        const name = nameInput ? nameInput.value.trim() : '';

        if (!name) {
            skylock.showToast('Please enter a folder name', 'error');
            return;
        }

        try {
            const body = { name: name };
            if (skylock.state.currentFolderId) body.parentId = skylock.state.currentFolderId;

            await skylock.apiFetch('/api/folders', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
            });

            skylock.showToast('Folder created successfully!', 'success');
            closeFolderModal();
            skylock.loadDashboard();
        } catch (e) {
            // Error already shown by apiFetch
        }
    }

    function closeFolderModal() {
        if (folderModal) folderModal.classList.remove('visible');
        const nameInput = document.getElementById('folder-name-input');
        if (nameInput) nameInput.value = '';
    }

})();
