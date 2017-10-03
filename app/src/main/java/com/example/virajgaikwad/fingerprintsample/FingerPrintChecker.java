package com.example.virajgaikwad.fingerprintsample;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

/**
 * Created by virajgaikwad on 5/22/17.
 */

class FingerPrintChecker {

    private final Context context;
    private final KeyguardManager keyguardManager;
    private final FingerprintManager fingerprintManager;


    public FingerPrintChecker(final Context context, FingerprintManager fingerprintManager) {
        this.context = context;
        this.fingerprintManager = fingerprintManager;

        keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
    }

    @SuppressWarnings("MissingPermissions")
    boolean isAbleToUseFingerPrint() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return false;
        }
        if (!fingerprintManager.isHardwareDetected()) {
            Toast.makeText(context, "Your device doesn't support fingerprint reader", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            if (isFingerPrintPermissionsEnabled()) {
                Toast.makeText(context, String.format(context.getString(R.string.error_permission_missing),
                        Manifest.permission.USE_FINGERPRINT), Toast.LENGTH_SHORT).show();
                return false;
            } else {
                if (!fingerprintManager.hasEnrolledFingerprints()) {
                    Toast.makeText(context, "Please register at least one fingerprint in your device settings", Toast.LENGTH_SHORT).show();
                    return false;
                } else {
                    if (!keyguardManager.isDeviceSecure()) {
                        Toast.makeText(context, "Lock screen security not enabled in your device settings", Toast.LENGTH_SHORT).show();
                        return false;
                    } else {
                        return true;
                    }
                }
            }
        }

    }

    private boolean isFingerPrintPermissionsEnabled() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED;
    }
}
