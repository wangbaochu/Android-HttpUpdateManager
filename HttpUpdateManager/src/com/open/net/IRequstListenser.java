package com.open.net;

public interface IRequstListenser {

	public void notNetConnection();

	public void error(Throwable err);

	public void handleData(String json);

	public void finish();
}
