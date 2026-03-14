/* =====================================================
   SkyLock — Move Modal (Organize Files & Folders)
   ===================================================== */

(function () {
    'use strict';

    const moveModal = document.getElementById('move-modal');
    let currentMoveType = null; // 'file' or 'folder'
    let currentMoveId = null;
    let currentMoveName = '';
    let selectedFolderId = null;
    let moveFolderStack = []; // [{id, name}] for breadcrumb navigation inside move modal

    // ── Open Move Modal ──────────────────────────────
    skylock.openMoveModal = function (type, id, name) {
        currentMoveType = type;
        currentMoveId = id;
        currentMoveName = name;
        selectedFolderId = null;
        moveFolderStack = [];

        document.getElementById('move-modal-item-name').textContent = name;
        loadFolderTree(null);

        if (moveModal) {
            moveModal.classList.add('visible');
        }
    };

    // ── Bind Move Modal ──────────────────────────────
    skylock.bindMoveModal = function () {
        const cancelBtn = document.getElementById('move-cancel-btn');
        const confirmBtn = document.getElementById('move-confirm-btn');

        if (cancelBtn) cancelBtn.addEventListener('click', closeMoveModal);

        if (moveModal) {
            moveModal.addEventListener('click', (e) => {
                if (e.target === moveModal) closeMoveModal();
            });
        }

        if (confirmBtn) {
            confirmBtn.addEventListener('click', confirmMove);
        }
    };

    // ── Load Folder Tree ─────────────────────────────
    async function loadFolderTree(parentId) {
        const folderList = document.getElementById('move-folder-list');
        folderList.innerHTML = `
            <div class="move-loading">
                <div class="move-loading-spinner"></div>
                <span>Loading folders...</span>
            </div>
        `;

        updateMoveBreadcrumbs();

        try {
            let url = '/api/folders/tree';
            if (parentId) url += '?parentId=' + encodeURIComponent(parentId);

            const data = await skylock.apiFetch(url);
            renderFolderList(data.folders || []);
        } catch (e) {
            // API not ready — show demo folders or empty state
            renderFolderList([]);
        }
    }

    // ── Render Folder List ───────────────────────────
    function renderFolderList(folders) {
        const folderList = document.getElementById('move-folder-list');
        folderList.innerHTML = '';

        // Filter out the item being moved if it's a folder
        const filteredFolders = folders.filter(f => {
            if (currentMoveType === 'folder' && f.id === currentMoveId) return false;
            return true;
        });

        if (filteredFolders.length === 0) {
            folderList.innerHTML = `
                <div class="move-empty-state">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                        <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
                    </svg>
                    <p>No subfolders here</p>
                </div>
            `;
            return;
        }

        filteredFolders.forEach(folder => {
            const item = document.createElement('div');
            item.className = 'move-folder-item';
            if (folder.id === selectedFolderId) {
                item.classList.add('selected');
            }

            item.innerHTML = `
                <div class="move-folder-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                        <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
                    </svg>
                </div>
                <span class="move-folder-name">${skylock.escapeHtml(folder.name)}</span>
                <button class="move-folder-open" title="Open folder">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <polyline points="9 18 15 12 9 6"/>
                    </svg>
                </button>
            `;

            // Click to select this folder as destination
            item.addEventListener('click', (e) => {
                if (e.target.closest('.move-folder-open')) return;
                document.querySelectorAll('.move-folder-item.selected')
                    .forEach(el => el.classList.remove('selected'));
                item.classList.add('selected');
                selectedFolderId = folder.id;
            });

            // Click the arrow to navigate into this folder
            const openBtn = item.querySelector('.move-folder-open');
            openBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                moveFolderStack.push({ id: folder.id, name: folder.name });
                selectedFolderId = null;
                loadFolderTree(folder.id);
            });

            folderList.appendChild(item);
        });
    }

    // ── Move Breadcrumbs ─────────────────────────────
    function updateMoveBreadcrumbs() {
        const breadcrumbs = document.getElementById('move-breadcrumbs');
        if (!breadcrumbs) return;

        let html = `<a href="javascript:void(0)" class="move-breadcrumb-link" data-index="-1">My Files</a>`;

        moveFolderStack.forEach((folder, index) => {
            html += `<span class="move-breadcrumb-sep">›</span>`;
            if (index === moveFolderStack.length - 1) {
                html += `<span class="move-breadcrumb-current">${skylock.escapeHtml(folder.name)}</span>`;
            } else {
                html += `<a href="javascript:void(0)" class="move-breadcrumb-link" data-index="${index}">${skylock.escapeHtml(folder.name)}</a>`;
            }
        });

        breadcrumbs.innerHTML = html;

        // Bind breadcrumb clicks
        breadcrumbs.querySelectorAll('.move-breadcrumb-link').forEach(link => {
            link.addEventListener('click', () => {
                const idx = parseInt(link.dataset.index, 10);
                if (idx === -1) {
                    moveFolderStack = [];
                    selectedFolderId = null;
                    loadFolderTree(null);
                } else {
                    const target = moveFolderStack[idx];
                    moveFolderStack = moveFolderStack.slice(0, idx + 1);
                    selectedFolderId = null;
                    loadFolderTree(target.id);
                }
            });
        });
    }

    // ── Confirm Move ─────────────────────────────────
    async function confirmMove() {
        // Determine the target folder ID
        const targetId = selectedFolderId ||
            (moveFolderStack.length > 0 ? moveFolderStack[moveFolderStack.length - 1].id : null);

        const endpoint = currentMoveType === 'folder'
            ? `/api/folders/${currentMoveId}/move`
            : `/api/files/${currentMoveId}/move`;

        try {
            await skylock.apiFetch(endpoint, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ targetFolderId: targetId }),
            });

            skylock.showToast(
                `${currentMoveType === 'folder' ? 'Folder' : 'File'} moved successfully!`,
                'success'
            );
            closeMoveModal();
            skylock.loadDashboard();
        } catch (e) {
            // Error already shown by apiFetch
        }
    }

    // ── Close ────────────────────────────────────────
    function closeMoveModal() {
        if (moveModal) moveModal.classList.remove('visible');
    }

})();
