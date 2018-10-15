/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/luoran/code/1012/Camera2/common/src/main/aidl/com/freeme/camera/common/location/ILocationSecurityService.aidl
 */
package com.freeme.camera.common.location;
// Declare any non-default types here with import statements

public interface ILocationSecurityService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.freeme.camera.common.location.ILocationSecurityService
{
private static final java.lang.String DESCRIPTOR = "com.freeme.camera.common.location.ILocationSecurityService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.freeme.camera.common.location.ILocationSecurityService interface,
 * generating a proxy if needed.
 */
public static com.freeme.camera.common.location.ILocationSecurityService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.freeme.camera.common.location.ILocationSecurityService))) {
return ((com.freeme.camera.common.location.ILocationSecurityService)iin);
}
return new com.freeme.camera.common.location.ILocationSecurityService.Stub.Proxy(obj);
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
case TRANSACTION_listenUserEvent:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
com.freeme.camera.common.location.ILocationListener _arg1;
_arg1 = com.freeme.camera.common.location.ILocationListener.Stub.asInterface(data.readStrongBinder());
java.lang.String _arg2;
_arg2 = data.readString();
int _arg3;
_arg3 = data.readInt();
this.listenUserEvent(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
return true;
}
case TRANSACTION_unregisterListener:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
com.freeme.camera.common.location.ILocationListener _arg1;
_arg1 = com.freeme.camera.common.location.ILocationListener.Stub.asInterface(data.readStrongBinder());
this.unregisterListener(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_getLocationStrFromSecurityByCoo:
{
data.enforceInterface(DESCRIPTOR);
double _arg0;
_arg0 = data.readDouble();
double _arg1;
_arg1 = data.readDouble();
java.lang.String _result = this.getLocationStrFromSecurityByCoo(_arg0, _arg1);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getCoordinates:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getCoordinates();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getLocationStrFromSecurity:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getLocationStrFromSecurity();
reply.writeNoException();
reply.writeString(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.freeme.camera.common.location.ILocationSecurityService
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
@Override public void listenUserEvent(java.lang.String key, com.freeme.camera.common.location.ILocationListener listener, java.lang.String pkgName, int verCode) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(key);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
_data.writeString(pkgName);
_data.writeInt(verCode);
mRemote.transact(Stub.TRANSACTION_listenUserEvent, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void unregisterListener(java.lang.String key, com.freeme.camera.common.location.ILocationListener listener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(key);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_unregisterListener, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public java.lang.String getLocationStrFromSecurityByCoo(double longitude, double latitude) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeDouble(longitude);
_data.writeDouble(latitude);
mRemote.transact(Stub.TRANSACTION_getLocationStrFromSecurityByCoo, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getCoordinates() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getCoordinates, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getLocationStrFromSecurity() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getLocationStrFromSecurity, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_listenUserEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_unregisterListener = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_getLocationStrFromSecurityByCoo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_getCoordinates = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_getLocationStrFromSecurity = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
}
public void listenUserEvent(java.lang.String key, com.freeme.camera.common.location.ILocationListener listener, java.lang.String pkgName, int verCode) throws android.os.RemoteException;
public void unregisterListener(java.lang.String key, com.freeme.camera.common.location.ILocationListener listener) throws android.os.RemoteException;
public java.lang.String getLocationStrFromSecurityByCoo(double longitude, double latitude) throws android.os.RemoteException;
public java.lang.String getCoordinates() throws android.os.RemoteException;
public java.lang.String getLocationStrFromSecurity() throws android.os.RemoteException;
}
