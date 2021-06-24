package cn.edu.thssdb.service;

import cn.edu.thssdb.rpc.thrift.ConnectReq;
import cn.edu.thssdb.rpc.thrift.ConnectResp;
import cn.edu.thssdb.rpc.thrift.DisconnetReq;
import cn.edu.thssdb.rpc.thrift.DisconnetResp;
import cn.edu.thssdb.rpc.thrift.ExecuteStatementReq;
import cn.edu.thssdb.rpc.thrift.ExecuteStatementResp;
import cn.edu.thssdb.rpc.thrift.GetTimeReq;
import cn.edu.thssdb.rpc.thrift.GetTimeResp;
import cn.edu.thssdb.rpc.thrift.IService;
import cn.edu.thssdb.rpc.thrift.Status;
import cn.edu.thssdb.schema.Manager;
import cn.edu.thssdb.statement.ExecuteResult;
import cn.edu.thssdb.statement.Executor;
import cn.edu.thssdb.transaction.Session;
import cn.edu.thssdb.utils.Global;
import org.apache.thrift.TException;

import java.util.Date;

public class IServiceHandler implements IService.Iface {

	private Manager manager = Manager.getInstance();

	@Override
  	public GetTimeResp getTime(GetTimeReq req) throws TException {
    	GetTimeResp resp = new GetTimeResp();
    	resp.setTime(new Date().toString());
    	resp.setStatus(new Status(Global.SUCCESS_CODE));
    	return resp;
  	}

  	@Override
  	public ConnectResp connect(ConnectReq req) throws TException {
		/* 获取用户名和密码 */
		String username = req.getUsername();
		String password = req.getPassword();

		/* 初始化Response */
		ConnectResp resp = new ConnectResp();	// 响应
		Status status = new Status();			// 状态

		if (username.isEmpty())	
		{
			/* 如果用户名为空，登录失败 */
			status.setCode(Global.FAILURE_CODE);
			status.setMsg(Global.NEED_USERNAME);
			resp.setStatus(status);
		}
		else
		{
			/* 登录成功 */
			status.setCode(Global.SUCCESS_CODE);
			status.setMsg(Global.LOGIN_OK);
			resp.setStatus(status);
			resp.setSessionId(manager.getConnectSession().getSessionId());
		}

    	return resp;
  	}

  	@Override
  	public DisconnetResp disconnect(DisconnetReq req) throws TException {
    	DisconnetResp resp = new DisconnetResp();
		Status status = new Status();
		long sessionId = req.getSessionId();
    	
		/* 检查用户是否已登录 */
		if (!manager.isConnected(sessionId))
		{
			status.setCode(Global.FAILURE_CODE);
			status.setMsg(Global.NEED_LOGIN);
			resp.setStatus(status);
		}
		else
		{
			status.setCode(Global.SUCCESS_CODE);
			status.setMsg(Global.LOGOUT_OK);
			resp.setStatus(status);
			manager.removeSession(sessionId);
		}

		return resp;
  	}

  	@Override
  	public ExecuteStatementResp executeStatement(ExecuteStatementReq req) throws TException {
    	/* 检查sessionId */
		long sessionId = req.getSessionId();
		Session session;
		ExecuteStatementResp resp = new ExecuteStatementResp();
		Status status = new Status();
		if (!manager.isConnected(sessionId))
		{
			status.setCode(Global.FAILURE_CODE);
			status.setMsg(Global.NEED_LOGIN);
			resp.setStatus(status);
			return resp;
		}
		else
		{
			session = manager.getSession(sessionId);
		}

		/* 处理语句 */
		String statement = req.getStatement();
		ExecuteResult res = Executor.execute(statement, session);
		status.setCode(res.getCode());
		status.setMsg(res.getMsg());
		resp.setStatus(status);
		if (!res.hasResult())
		{
			resp.setHasResult(false);
			return resp;
		}
		else
		{
			resp.setHasResult(true);
			resp.setColumnsList(res.getColumns());
			resp.setRowList(res.getRows());
			return resp;
		}
  	}
}
