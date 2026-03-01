/* =====================================================
   SkyLock — Navigation (Sidebar + Breadcrumbs)
   ===================================================== */

(function () {
    'use strict';

    // ── Sidebar Nav ──────────────────────────────────
    skylock.bindSidebarNav = function () {
        const navItems = document.querySelectorAll('.nav-item');
        navItems.forEach(item => {
            item.addEventListener('click', function () {
                navItems.forEach(n => n.classList.remove('active'));
                this.classList.add('active');

                if (this.dataset.nav === 'home' || this.dataset.nav === 'projects') {
                    skylock.state.folderStack = [];
                    skylock.state.currentFolderId = null;
                    skylock.closeInspector();
                    skylock.loadDashboard();
                }
            });
        });
    };

    // ── Breadcrumbs ──────────────────────────────────
    skylock.updateBreadcrumbs = function () {
        const breadcrumbs = document.getElementById('breadcrumbs');
        if (!breadcrumbs) return;

        let html = `<a href="javascript:void(0)" onclick="skylock.goToRoot()">Home</a>`;

        if (skylock.state.folderStack.length > 0) {
            html += `<span class="sep">/</span>`;
            html += `<a href="javascript:void(0)" onclick="skylock.goToRoot()">My Files</a>`;

            skylock.state.folderStack.forEach((folder, index) => {
                html += `<span class="sep">/</span>`;
                if (index === skylock.state.folderStack.length - 1) {
                    html += `<span class="current">${skylock.escapeHtml(folder.name)}</span>`;
                } else {
                    html += `<a href="javascript:void(0)" onclick="skylock.goToFolder(${index})">${skylock.escapeHtml(folder.name)}</a>`;
                }
            });
        } else {
            html += `<span class="sep">/</span>`;
            html += `<span class="current">My Files</span>`;
        }

        breadcrumbs.innerHTML = html;
    };

    skylock.updateContentHeader = function () {
        const header = document.querySelector('.content-header h1');
        if (!header) return;

        if (skylock.state.folderStack.length > 0) {
            header.textContent = skylock.state.folderStack[skylock.state.folderStack.length - 1].name;
        } else {
            header.textContent = 'My Files';
        }
    };

    // ── Folder Stack Navigation ──────────────────────
    skylock.goToRoot = function () {
        skylock.state.folderStack = [];
        skylock.state.currentFolderId = null;
        skylock.closeInspector();
        skylock.loadDashboard();
    };

    skylock.goToFolder = function (index) {
        const target = skylock.state.folderStack[index];
        skylock.state.folderStack = skylock.state.folderStack.slice(0, index + 1);
        skylock.state.currentFolderId = target.id;
        skylock.closeInspector();
        skylock.loadDashboard();
    };

    skylock.goBack = function () {
        if (skylock.state.folderStack.length > 0) {
            skylock.state.folderStack.pop();
            if (skylock.state.folderStack.length > 0) {
                skylock.state.currentFolderId =
                    skylock.state.folderStack[skylock.state.folderStack.length - 1].id;
            } else {
                skylock.state.currentFolderId = null;
            }
            skylock.closeInspector();
            skylock.loadDashboard();
        }
    };

})();
