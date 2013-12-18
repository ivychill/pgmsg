package com.luyun.msg;

public interface MsgListener {
	public abstract void onCheckin(Checkin checkin);
	public abstract void onTrans(String trans);
}
