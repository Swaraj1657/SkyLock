/* =====================================================
   SkyLock CloudCmd — App Init
   Loads after all module scripts. Binds events & boots.
   ===================================================== */

document.addEventListener('DOMContentLoaded', () => {
    // Check for deep links
    const params = new URLSearchParams(window.location.search);
    const view = params.get('view');
    const fileId = params.get('fileId');

    if (view === 'shared') {
        skylock.state.viewMode = 'shared';
        // Highlight shared tab in sidebar
        const sharedBtn = document.querySelector('.nav-item[data-nav="shared"]');
        if (sharedBtn) {
            document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
            sharedBtn.classList.add('active');
        }
        skylock.loadSharedItems(fileId);
    } else {
        skylock.loadDashboard();
    }

    skylock.bindSidebarNav();
    skylock.bindUploadModal();
    skylock.bindFolderModal();
    skylock.bindShareModal();
    skylock.bindRenameModal();
    skylock.bindMoveModal();
    skylock.bindUserMenu();
    skylock.bindStorageRing();
});
