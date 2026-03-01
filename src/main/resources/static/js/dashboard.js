/* =====================================================
   SkyLock — Dashboard & Grid Rendering
   ===================================================== */

(function () {
    'use strict';

    // ── Shared State ─────────────────────────────────
    skylock.state = {
        selectedFileId: null,
        currentFolderId: null,
        dashboardData: null,
        folderStack: []   // [{id, name}, ...] for breadcrumbs
    };

    const assetGrid = document.getElementById('asset-grid');

    // ── Dashboard Load ───────────────────────────────
    skylock.loadDashboard = async function () {
        let url = '/api/dashboard';
        if (skylock.state.currentFolderId)
            url += '?folderId=' + encodeURIComponent(skylock.state.currentFolderId);

        try {
            skylock.state.dashboardData = await skylock.apiFetch(url);
            renderGrid(skylock.state.dashboardData.files, skylock.state.dashboardData.folders);
            skylock.updateStorage(skylock.state.dashboardData.storage);
            skylock.updateBreadcrumbs();
            skylock.updateContentHeader();
        } catch (e) {
            renderEmptyState('Failed to load files.');
        }
    };

    // ── Render Grid ──────────────────────────────────
    function renderGrid(files, folders) {
        assetGrid.innerHTML = '';

        if ((!files || files.length === 0) && (!folders || folders.length === 0)) {
            renderEmptyState('No files yet. Click "New Upload" or "New Folder" to get started.');
            return;
        }

        let index = 0;

        if (folders) {
            folders.forEach(folder => {
                const card = createFolderCard(folder, index);
                assetGrid.appendChild(card);
                index++;
            });
        }

        if (files) {
            files.forEach(file => {
                const card = createFileCard(file, index);
                assetGrid.appendChild(card);
                index++;
            });
        }
    }

    function createFolderCard(folder, index) {
        const card = document.createElement('div');
        card.className = 'asset-card';
        card.style.animationDelay = `${index * 0.06}s`;
        card.dataset.folderId = folder.id;

        card.innerHTML = `
            <div class="card-thumb">
                <span class="folder-icon">📁</span>
            </div>
            <div class="card-info">
                <h3>${skylock.escapeHtml(folder.name)}</h3>
                <span class="card-meta">${folder.itemCount || 0} items · Folder</span>
            </div>
        `;

        card.addEventListener('click', () => {
            skylock.state.folderStack.push({ id: folder.id, name: folder.name });
            skylock.state.currentFolderId = folder.id;
            skylock.state.selectedFileId = null;
            skylock.closeInspector();
            skylock.loadDashboard();
        });

        return card;
    }

    function createFileCard(file, index) {
        const card = document.createElement('div');
        card.className = 'asset-card';
        card.style.animationDelay = `${index * 0.06}s`;
        card.dataset.fileId = file.id;

        const thumbContent = getThumbContent(file);

        card.innerHTML = `
            <div class="card-thumb">
                ${thumbContent}
                <div class="card-actions">
                    <button class="card-action-btn edit" title="Edit" onclick="event.stopPropagation()">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                        </svg>
                    </button>
                    <button class="card-action-btn share" title="Share" onclick="event.stopPropagation()">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/>
                            <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/>
                            <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/>
                        </svg>
                    </button>
                    <button class="card-action-btn delete" title="Delete" data-file-id="${file.id}" onclick="event.stopPropagation(); skylock.deleteFile('${file.id}')">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <polyline points="3 6 5 6 21 6"/>
                            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                        </svg>
                    </button>
                </div>
            </div>
            <div class="card-info">
                <h3>${skylock.escapeHtml(file.filename)}</h3>
                <span class="card-meta">${skylock.formatSize(file.size)} · Updated ${skylock.formatDate(file.uploadedAt)}</span>
            </div>
        `;

        card.addEventListener('click', () => skylock.selectFile(file, card));

        return card;
    }

    function getThumbContent(file) {
        const icons = {
            image: '🖼️', pdf: '📄', document: '📝',
            spreadsheet: '📊', video: '🎬', audio: '🎵',
            archive: '📦', file: '📎'
        };
        const icon = icons[file.type] || icons.file;
        return `<span class="file-icon">${icon}</span>`;
    }

    // ── Empty State ──────────────────────────────────
    function renderEmptyState(msg) {
        assetGrid.innerHTML = `
            <div class="empty-state" style="grid-column: 1 / -1;">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
                </svg>
                <h3>No files here</h3>
                <p>${msg}</p>
            </div>
        `;
    }

    // ── Delete File ──────────────────────────────────
    skylock.deleteFile = async function (fileId) {
        if (!confirm('Are you sure you want to delete this file?')) return;

        try {
            await skylock.apiFetch('/api/files/' + fileId, { method: 'DELETE' });
            skylock.showToast('File deleted successfully', 'success');
            if (skylock.state.selectedFileId === fileId) skylock.closeInspector();
            skylock.loadDashboard();
        } catch (e) {
            // Error already shown by apiFetch
        }
    };

})();
