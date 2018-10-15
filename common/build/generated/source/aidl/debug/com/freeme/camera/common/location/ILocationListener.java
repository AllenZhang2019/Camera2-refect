/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/luoran/code/1012/Camera2/common/src/main/aidl/com/freeme/camera/common/location/ILocationListener.aidl
 */
package com.freeme.camera.common.location;
// Declare any non-default types here with import statements

public interface ILocationListener extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.freeme.camera.common.location.ILocationListener
{
private static final java.lang.String DESCRIPTOR = "com.freeme.camera.common.location.ILocationListener";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.freeme.camera.common.location.ILocationListener interface,
 * generating a proxy if needed.
 */
public static com.freeme.camera.common.location.ILocationListener asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.freeme.camera.common.location.ILocationListener))) {
return ((com.freeme.camera.common.location.ILocationListener)iin);
}
return new com.freeme.camera.common.location.ILocationListener.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_getLocationStrByCooCompleted:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.getLocationStrByCooCompleted(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getLocationStrCompleted:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.getLocationStrCompleted(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getCoordinatesCompleted:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.getCoordinatesCompleted(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.freeme.camera.common.location.ILocationListener
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void getLocationStrByCooCompleted(java.lang.String locationInfo) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(locationInfo);
mRemote.transact(Stub.TRANSACTION_getLocationStrByCooCompleted, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void getLocationStrCompleted(java.lang.String locationInfo) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(locationInfo);
mRemote.transact(Stub.TRANSACTION_getLocationStrCompleted, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void getCoordinatesCompleted(java.lang.String locationInfo) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(locationInfo);
mRemote.transact(Stub.TRANSACTION_getCoordinatesCompleted, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_getLocationStrByCooCompleted = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_getLocationStrCompleted = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_getCoordinatesCompleted = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
public void getLocationStrByCooCompleted(java.lang.String locationInfo) throws android.os.RemoteException;
public void getLocationStrCompleted(java.lang.String locationInfo) throws android.os.RemoteException;
public void getCoordinatesCompleted(java.lang.String locationInfo) throws android.os.RemoteException;
}
