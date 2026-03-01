/* =====================================================
   SkyLock — User Menu (Avatar Dropdown)
   ===================================================== */

(function () {
    'use strict';

    skylock.bindUserMenu = function () {
        const avatarBtn = document.getElementById('avatar-btn');
        const dropdown = document.getElementById('user-dropdown');

        if (avatarBtn && dropdown) {
            avatarBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                dropdown.classList.toggle('open');
            });

            document.addEventListener('click', () => {
                dropdown.classList.remove('open');
            });
        }
    };

})();
