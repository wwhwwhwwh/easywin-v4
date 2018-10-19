package com.westar.core.web.controller;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.westar.base.cons.SessionKeyConstant;
import com.westar.base.model.UserInfo;
import com.westar.base.pojo.Notification;
import com.westar.base.util.ConstantInterface;
import com.westar.base.util.CusAccessObjectUtil;
import com.westar.base.util.Encodes;
import com.westar.base.util.StringUtil;
import com.westar.core.service.UserInfoService;
import com.westar.core.web.SessionContext;

@Controller
public class LoginController extends BaseController {

	@Autowired
	UserInfoService userInfoService;

	/**
	 * 登录
	 * 
	 * @param session
	 *            浏览器session
	 * @param loginName
	 *            账号
	 * @param isSysUser
	 *            是否为已有的系统成员 1是0否
	 * @param password
	 *            密码
	 * @param comId
	 *            团队号
	 * @param request
	 *            本次访问请求
	 * @param cookieType
	 *            缓存类型
	 * @param yzm
	 *            验证码
	 * @param autoLogin
	 *            是否为自动登录
	 * @return
	 */
	@RequestMapping("/login")
	public ModelAndView login(HttpSession session, String loginName, String isSysUser, String password, String comId,
			HttpServletRequest request, String cookieType, String yzm, String autoLogin) {
		// 操作的IP
		String optIP = CusAccessObjectUtil.getIpAddress(request);

		String passwordMD5 = password;
		// 没有采用客户端数据
		if (StringUtil.isBlank(cookieType) || !"no".equals(cookieType)) {
			String rand = (String) session.getAttribute("login_rand");
			if (null != rand && null != yzm && yzm.equalsIgnoreCase(rand)) {
				session.removeAttribute("login_rand");
			} else {
				this.setNotification(Notification.ERROR, "验证码失效，请重新登录");
				return new ModelAndView("redirect:/login.jsp");
			}
			passwordMD5 = Encodes.encodeMd5(password);
		}
		// 是已有的成员系统
		final UserInfo userInfo = userInfoService.userAuth(loginName.toLowerCase(), passwordMD5, comId);
		if (userInfo != null && userInfo.getInService() == ConstantInterface.USER_INSERVICE_YES) {
			// 设置操作IP
			userInfo.setOptIP(optIP);

			String sid = this.setSessionObj(SessionKeyConstant.USER_CONTEXT, userInfo);
			SessionContext.removeSessionUser(userInfo.getId());
			SessionContext.addSessionUser(userInfo.getId(), sid, session);
			ModelAndView view = new ModelAndView("/toIndex", "sid", sid);
			view.addObject("userInfo", userInfo);

			view.addObject("autoLogin", autoLogin);
			// 用户主键
			view.addObject("id", Encodes.encodeBase64(userInfo.getId().toString()).trim());
			// 登录名称
			view.addObject("loginName", Encodes.encodeBase64(loginName).trim());

			if (StringUtil.isBlank(cookieType) || !"no".equals(cookieType)) {// 没有采用客户端本地保存的数据
				// 设置了保存登录
				if ("yes".equals(autoLogin)) {
					// 企业号
					view.addObject("comId", Encodes.encodeBase64(comId).trim());
					// 自动登录加密
					view.addObject("autoLoginP", Encodes.encodeBase64("yes").trim());
					// 自动登录没有加密
					view.addObject("autoLogin", "yes");
					// 用户加密后的密码
					view.addObject("password", Encodes.encodeBase64(passwordMD5).trim());

				} else {
					// 不自动登录
					view.addObject("autoLoginP", Encodes.encodeBase64("no").trim());
				}
			}
			// 若是自动登录的，则不用再重新设置客户端保存本地的数据
			view.addObject("cookieType", cookieType);

			view.addObject("addInfo", "n");
			view.addObject("isSysUser", ConstantInterface.USER_INSERVICE_YES.toString());
			return view;
		} else if (userInfo != null && userInfo.getInService() == ConstantInterface.USER_INSERVICE_NO) {
			this.setNotification(Notification.ERROR, "登录失败，你的账号超出团队人数上限，请联系你们的团队管理员。");
			return new ModelAndView("redirect:/login.jsp");
		} else {
			this.setNotification(Notification.ERROR, "登录失败，请检查你的账号和密码。");
			return new ModelAndView("redirect:/login.jsp");
		}

	}

	/**
	 * 注销
	 * 
	 * @return
	 */
	@RequestMapping("/exit")
	public ModelAndView exit(HttpSession session, String sid) {
		UserInfo userInfo = this.getSessionUser();
		ModelAndView view = new ModelAndView("redirect:/logout.jsp");
		view.addObject("sid", sid);
		return view;
	}

	/**
	 * 跳转注册页面(此方法暂是不用)
	 * 
	 * @return
	 */
	@RequestMapping("/login/newUser")
	public ModelAndView newUser() {
		return new ModelAndView("redirect:/newUser.jsp");
	}

	/**
	 * 检测系统中设置了自动登录后选择企业的情况
	 * 
	 * @param request
	 * @param l_c_i_p_m
	 *            加密后的登录名称
	 * @param c_i_p_m_l
	 *            加密后的企业号
	 * @param i_p_m_l_c
	 *            加密后的用户主键
	 * @param p_m_l_c_i
	 *            加密后的密码
	 * @param autologin
	 *            設置自動登錄沒有
	 * @return
	 * @throws UnknownHostException
	 * @throws SocketException
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/login/checkCookies")
	public Map<String, Object> checkCookies(HttpServletRequest request, String l_c_i_p_m, String c_i_p_m_l,
			String i_p_m_l_c, String p_m_l_c_i, String autologin) throws SocketException, UnknownHostException {
		Map<String, Object> map = new HashMap<String, Object>();
		if (null != l_c_i_p_m && null != c_i_p_m_l && null != i_p_m_l_c && null != autologin) {
			if (!Encodes.decodeBase64(autologin).equalsIgnoreCase("yes")) {// 没有设置自动登录
				map.put("state", "n");
				return map;
			}

			// 邮箱或是别名
			String account = Encodes.decodeBase64(l_c_i_p_m);
			// 企业号
			String comId = Encodes.decodeBase64(c_i_p_m_l);
			// 用户主键
			String id = Encodes.decodeBase64(i_p_m_l_c);
			// 加密后的密码
			String password = Encodes.decodeBase64(p_m_l_c_i);


		}

		return map;
	}

	/**
	 * 用于登陆选择企业
	 * 
	 * @return
	 */
	@RequestMapping(value = "/login/listChooseOrg")
	public ModelAndView listChooseOrg() {
		ModelAndView mav = new ModelAndView("listChooseOrg");
		return mav;
	}

}