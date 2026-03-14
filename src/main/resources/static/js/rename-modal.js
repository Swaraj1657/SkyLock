/* =====================================================
   SkyLock — Rename Modal (Files & Folders)
   ===================================================== */

(function () {
    'use strict';

    const renameModal = document.getElementById('rename-modal');
    let currentRenameType = null; // 'file' or 'folder'
    let currentRenameId = null;

    // ── Open Rename Modal ─────────────────────────────
    skylock.openRenameModal = function (type, id, currentName) {
        currentRenameType = type;
        currentRenameId = id;

        const titleEl = document.getElementById('rename-modal-title');
        const input = document.getElementById('rename-input');

        titleEl.textContent = type === 'folder' ? 'Rename Folder' : 'Rename File';
        input.value = currentName || '';

        if (renameModal) {
            renameModal.classList.add('visible');
            // Select filename without extension for files
            setTimeout(() => {
                if (type === 'file' && currentName) {
                    const dotIndex = currentName.lastIndexOf('.');
                    if (dotIndex > 0) {
                        input.setSelectionRange(0, dotIndex);
                    } else {
                        input.select();
                    }
                } else {
                    input.select();
                }
                input.focus();
            }, 100);
        }
    };

    // ── Bind Rename Modal ─────────────────────────────
    skylock.bindRenameModal = function () {
        const cancelBtn = document.getElementById('rename-cancel-btn');
        const confirmBtn = document.getElementById('rename-confirm-btn');
        const input = document.getElementById('rename-input');

        if (cancelBtn) cancelBtn.addEventListener('click', closeRenameModal);

        if (renameModal) {
            renameModal.addEventListener('click', (e) => {
                if (e.target === renameModal) closeRenameModal();
            });
        }

        if (input) {
            input.addEventListener('keydown', (e) => {
                if (e.key === 'Enter') confirmRename();
                if (e.key === 'Escape') closeRenameModal();
            });
        }

        if (confirmBtn) {
            confirmBtn.addEventListener('click', confirmRename);
        }
    };

    // ── Confirm Rename ───────────────────────────────
    async function confirmRename() {
        const input = document.getElementById('rename-input');
        const newName = input.value.trim();

        if (!newName) {
            skylock.showToast('Please enter a name', 'error');
            return;
        }

        const endpoint = currentRenameType === 'folder'
            ? `/api/folders/${currentRenameId}/rename`
            : `/api/files/${currentRenameId}/rename`;

        try {
            await skylock.apiFetch(endpoint, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name: newName }),
            });

            skylock.showToast(
                `${currentRenameType === 'folder' ? 'Folder' : 'File'} renamed successfully!`,
                'success'
            );
            closeRenameModal();
            skylock.loadDashboard();
        } catch (e) {
            // Error already shown by apiFetch
        }
    }

    // ── Close ────────────────────────────────────────
    function closeRenameModal() {
        if (renameModal) renameModal.classList.remove('visible');
    }

})();
