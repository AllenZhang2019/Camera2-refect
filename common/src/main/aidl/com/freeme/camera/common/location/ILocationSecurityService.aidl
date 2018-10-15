// ILocationSecurityService.aidl
package com.freeme.camera.common.location;
import com.freeme.camera.common.location.ILocationListener;

// Declare any non-default types here with import statements

interface ILocationSecurityService {
    void listenUserEvent(String key,ILocationListener listener, String pkgName, int verCode);
    void unregisterListener(String key,ILocationListener listener);
    String getLocationStrFromSecurityByCoo(double longitude,double latitude);
    String getCoordinates();
    String getLocationStrFromSecurity();
}