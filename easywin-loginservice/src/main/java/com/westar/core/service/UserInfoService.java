package com.westar.core.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.westar.base.model.Organic;
import com.westar.base.model.UserInfo;
import com.westar.base.util.Encodes;
import com.westar.base.util.PinyinToolkit;
import com.westar.base.util.StringUtil;
import com.westar.core.dao.UserInfoDao;

@Service
public class UserInfoService {

	@Autowired
	UserInfoDao userInfoDao;

	/**
	 * 查询用户分页列表 分页查询
	 * 
	 * @param userInfo
	 * @return 人员信息 集合
	 */
	public List<UserInfo> listPagedUserInfo(UserInfo userInfo) {
		List<UserInfo> list = userInfoDao.listPagedUserInfo(userInfo);
		return list;
	}
	
	/**
	 * 查询用户分页列表不分页
	 * 
	 * @param userInfo
	 * @return 人员信息 集合
	 */
	public List<UserInfo> listUserInfo(UserInfo userInfo) {
		List<UserInfo> list = userInfoDao.listUserInfo(userInfo);
		return list;
	}

	/**
	 * 新增用户 新增数据
	 * 
	 * @param userInfo
	 * @return 返回主键ID
	 */
	public Integer addUserInfo(UserInfo userInfo) {
		// 注册添加企业主键
		Organic organic = new Organic();
		// 公司编号
		organic.setOrgNum(userInfo.getComId());
		organic.setOrgName(userInfo.getOrgName());
		// 激活邮箱后启用
		organic.setEnabled("0");
		userInfoDao.add(organic);
		// 激活邮箱后启用
		userInfo.setEnabled("0");
		// 激活邮箱后审核通过
		userInfo.setCheckState("0");
		// 管理人员
		userInfo.setAdmin("1");
		// 用户名称不为空时此时添加用户名称全拼和简拼
		if (null != userInfo.getUserName()) {
			userInfo.setAllSpelling(PinyinToolkit.cn2Spell(userInfo
					.getUserName()));
			userInfo.setFirstSpelling(PinyinToolkit.cn2FirstSpell(userInfo
					.getUserName()));
		}
		String passwordMD5 = Encodes.encodeMd5(userInfo.getPassword());
		userInfo.setPassword(passwordMD5);
		Integer userId = userInfoDao.add(userInfo);
		return userId;
	}
	

	/**
	 * 修改用户 更新修改
	 * 
	 * @param userInfo
	 */
	public void updateUserInfo(UserInfo userInfo) {

		if (!"".equals(StringUtil.delNull(userInfo.getUserName()))) {
			userInfo.setAllSpelling(PinyinToolkit.cn2Spell(userInfo
					.getUserName()));
			userInfo.setFirstSpelling(PinyinToolkit.cn2FirstSpell(userInfo
					.getUserName()));
		}
		userInfoDao.update(userInfo);
	}

	/**
	 * 修改用户 更新修改 只更新用户表信息 其他相关联信息不管
	 * 
	 * @param userInfo
	 */
	public void updateUserInfoOnly(UserInfo userInfo) {
		userInfoDao.update(userInfo);
	}


	/**
	 * 验证用户账号唯一性
	 * 
	 * @param loginName
	 *            登录名
	 * @return 账号唯一返回true
	 */
	public boolean validateLoginName(String loginName) {
		return userInfoDao.validateLoginName(loginName);
	}

	/**
	 * 根据账号和密码查找用户
	 * 
	 * @param loginName
	 * @param password
	 * @param comId
	 * @return
	 */
	public UserInfo userAuth(String loginName, String password, String comId) {
		UserInfo userInfo = userInfoDao.userAuth(loginName.toLowerCase(),
				password, comId);
		return userInfo;
	}


	/**
	 * 查询人员信息
	 * 
	 * @param userInfo
	 * @return
	 */
	public List<UserInfo> listUser(UserInfo userInfo) {
		return userInfoDao.listUser(userInfo);
	}

	/**
	 * 查询当前在线人数
	 * 
	 * @return 在线人数
	 */
	public int countOnlineUser() {
		return userInfoDao.countOnlineUser();
	}

	

	/**
	 * 更新背景皮肤
	 * 
	 * @param userInfo
	 */
	public void updateUserInfoSkin(UserInfo userInfo) {
		userInfoDao.update(userInfo);
	}

	/**
	 * 添加新企业用户
	 * 
	 * @param organic
	 */
	public void addOrganic(Organic organic) {
		userInfoDao.add(organic);
	}

	/**
	 * 新企业用户注册主键验证
	 * 
	 * @param key
	 * @return
	 */
	public boolean orgKeyCheck(Integer key) {
		return userInfoDao.orgKeyCheck(key);
	}

	/**
	 * 修改密码前验证密码
	 * 
	 * @param id
	 * @param passwordMD5
	 *            加密后的密码
	 * @return
	 */
	public boolean validatePassword(String id, String passwordMD5) {
		return userInfoDao.validatePassword(id, passwordMD5);
	}

	/**
	 * 查询部门的用户列表
	 * 
	 * @param userInfo
	 * @return
	 */
	public List<UserInfo> listPagedUserForDep(UserInfo userInfo) {
		List<UserInfo> list = userInfoDao.listPagedUserForDep(userInfo);
		return list;
	}

	/**
	 * 查询分组的用户列表
	 * 
	 * @param userInfo
	 * @param grpId
	 * @return
	 */
	public List<UserInfo> listPagedUserForGrp(UserInfo userInfo, String grpId) {
		List<UserInfo> list = userInfoDao.listPagedUserForGrp(userInfo, grpId);
		return list;
	}
	/**
	 * 添加非管理人员
	 * 
	 * @param userInfo
	 */
	public void addUserinfoOnly(UserInfo userInfo) {
		userInfoDao.add(userInfo);

	}

	/**
	 * 验证系统中是否存在用户
	 * 
	 * @param account
	 * @return
	 */
	public UserInfo getUserInfoByAccount(String account) {
		UserInfo user = userInfoDao.getUserInfoByAccount(account);
		return user;
	}


}
