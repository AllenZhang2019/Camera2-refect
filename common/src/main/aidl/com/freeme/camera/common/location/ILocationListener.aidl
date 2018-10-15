// ILocationListener.aidl
package com.freeme.camera.common.location;

// Declare any non-default types here with import statements

interface ILocationListener{
    void getLocationStrByCooCompleted(String locationInfo);
    void getLocationStrCompleted(String locationInfo);
    void getCoordinatesCompleted(String locationInfo);
}
