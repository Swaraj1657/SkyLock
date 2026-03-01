/* =====================================================
   SkyLock CloudCmd — App Init
   Loads after all module scripts. Binds events & boots.
   ===================================================== */

document.addEventListener('DOMContentLoaded', () => {
    skylock.loadDashboard();
    skylock.bindSidebarNav();
    skylock.bindUploadModal();
    skylock.bindFolderModal();
    skylock.bindUserMenu();
    skylock.bindStorageRing();
});
