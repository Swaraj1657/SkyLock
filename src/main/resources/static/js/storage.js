/* =====================================================
   SkyLock — Storage Ring Widget
   ===================================================== */

(function () {
    'use strict';

    skylock.bindStorageRing = function () {
        // Will be populated after dashboard load
    };

    skylock.updateStorage = function (storage) {
        const ringFill = document.getElementById('ring-fill');
        const ringText = document.getElementById('ring-text');
        const storageUsed = document.getElementById('storage-used');

        if (!storage) return;

        const pct = storage.percentage || 0;
        const circumference = 2 * Math.PI * 17; // r=17

        if (ringFill) {
            ringFill.style.strokeDasharray = circumference;
            ringFill.style.strokeDashoffset = circumference;
            setTimeout(() => {
                ringFill.style.strokeDashoffset = circumference - (circumference * pct / 100);
            }, 300);
        }

        if (ringText) ringText.textContent = pct + '%';
        if (storageUsed) storageUsed.textContent = skylock.formatSize(storage.used) +
            ' of ' + skylock.formatSize(storage.max);
    };

})();
