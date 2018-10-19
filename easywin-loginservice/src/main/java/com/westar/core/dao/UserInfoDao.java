package com.westar.core.dao;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import com.westar.base.model.Organic;
import com.westar.base.model.UserInfo;
import com.westar.base.util.ConstantInterface;
import com.westar.base.util.DateTimeUtil;
import com.westar.base.util.StringUtil;

@Repository
public class UserInfoDao extends BaseDao {

	/**
	 * 查询用户分页列表  分页查询
	 * @param userInfo
	 * @return 人员信息集合
	 */
	@SuppressWarnings("unchecked")
	public List<UserInfo> listPagedUserInfo(UserInfo userInfo) {
		String nowtime = DateTimeUtil.getNowDateStr(DateTimeUtil.yyyy_MM_dd_HH_mm_ss);
		/* 40秒前更新过在线时间的表示在线 */
		String cxtime = DateTimeUtil.addDate(nowtime, DateTimeUtil.yyyy_MM_dd_HH_mm_ss, Calendar.SECOND, -40);
		List<Object> args = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append("\n select a.id,case when a.userName is null then a.email else a.userName end as userName,");
		sql.append("\n a.allSpelling,a.firstSpelling,a.password,a.linePhone,a.movePhone,");
		sql.append("\n a.email,a.wechat,a.qq,aa.bigHeadPortrait,aa.mediumHeadPortrait,aa.smallHeadPortrait,a.gender,");
		sql.append("\n a.birthday,a.nickname,aa.comId,aa.admin,aa.job,aa.depId,aa.enabled,aa.lastOnlineTime,d.depName,");
		sql.append("\n aa.jifenScore,aa.id as userOrganicId ,b.orgName,c.uuid as smImgUuid,c.fileName as smImgName,");
		sql.append("\n case when aa.admin=0 then 0 when aa.admin=1 then 2 else 1 end as neworder,aa.inservice,aa.enrollNumber,");
		//当没有角色时默认为普通人员
		sql.append("\n case when binding.roles is null then '普通人员' else binding.roles end as roles");
		sql.append("\n from userinfo a inner join userOrganic aa on a.Id=aa.userId");
		sql.append("\n inner join organic b on aa.comId=b.orgNum");
		sql.append("\n left join upfiles c on aa.smallHeadPortrait=c.id and c.comId=aa.comId");
		sql.append("\n left join department d on aa.depId=d.id and d.comId=aa.comId");
		sql.append("\n left join (select a.userId,wm_concat(b.roleName) as roles from roleBindingUser a left join role b on a.roleId = b.id group by a.userId) binding on a.id = binding.userId");
		sql.append("\n where 1=1 ");
		this.addSqlWhere(userInfo.getComId(), sql, args, " and aa.comId=?");
		if(null!=userInfo.getUserName()){
			this.addSqlWhereLike(userInfo.getUserName().toLowerCase(), sql, args, "and (lower(a.email) like ? or lower(a.userName) like ? or lower(a.allSpelling) like ? or lower(a.firstSpelling) like ?)",4);
		}
		// 1表示查询在线的
		if (!StringUtil.isBlank(userInfo.getIfOnline())) {
			sql.append("\n and aa.lastOnlineTime>?");
			args.add(cxtime);
		}
		return this.pagedQuery(sql.toString()," neworder desc,aa.enabled desc,aa.inservice desc,aa.id", args.toArray(), UserInfo.class);
	}
	
	/**
	 * 查询用户分页列表  不分页
	 * @param userInfo
	 * @return 人员信息集合
	 */
	@SuppressWarnings("unchecked")
	public List<UserInfo> listUserInfo(UserInfo userInfo) {
		String nowtime = DateTimeUtil.getNowDateStr(DateTimeUtil.yyyy_MM_dd_HH_mm_ss);
		/* 40秒前更新过在线时间的表示在线 */
		String cxtime = DateTimeUtil.addDate(nowtime, DateTimeUtil.yyyy_MM_dd_HH_mm_ss, Calendar.SECOND, -40);
		List<Object> args = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append("\n select a.id,case when a.userName is null then a.email else a.userName end as userName,");
		sql.append("\n a.allSpelling,a.firstSpelling,a.password,a.linePhone,a.movePhone,");
		sql.append("\n a.email,a.wechat,a.qq,aa.bigHeadPortrait,aa.mediumHeadPortrait,aa.smallHeadPortrait,a.gender,");
		sql.append("\n a.birthday,a.nickname,aa.comId,aa.admin,aa.job,aa.depId,aa.enabled,aa.lastOnlineTime,d.depName,");
		sql.append("\n aa.jifenScore,aa.id as userOrganicId ,b.orgName,c.uuid as smImgUuid,c.fileName as smImgName,");
		sql.append("\n case when aa.admin=0 then 0 when aa.admin=1 then 2 else 1 end as neworder,aa.inservice,aa.enrollNumber ");
		sql.append("\n from userinfo a inner join userOrganic aa on a.Id=aa.userId");
		sql.append("\n inner join organic b on aa.comId=b.orgNum");
		sql.append("\n left join upfiles c on aa.smallHeadPortrait=c.id and c.comId=aa.comId");
		sql.append("\n left join department d on aa.depId=d.id and d.comId=aa.comId");
		sql.append("\n where 1=1 ");
		this.addSqlWhere(userInfo.getComId(), sql, args, " and aa.comId=?");
		if(null!=userInfo.getUserName()){
			this.addSqlWhereLike(userInfo.getUserName().toLowerCase(), sql, args, "and (lower(a.email) like ? or lower(a.userName) like ? or lower(a.allSpelling) like ? or lower(a.firstSpelling) like ?)",4);
		}
		// 1表示查询在线的
		if (!StringUtil.isBlank(userInfo.getIfOnline())) {
			sql.append("\n and aa.lastOnlineTime>?");
			args.add(cxtime);
		}
		sql.append("\n order by neworder desc,aa.enabled desc,aa.inservice desc,aa.id");
		return this.listQuery(sql.toString(), args.toArray(), UserInfo.class);
	}

	/**
	 * 查询当前在线人数
	 * @return 在线人数
	 */
	public int countOnlineUser() {
		int n = 0;
		String nowtime = DateTimeUtil.getNowDateStr(DateTimeUtil.yyyy_MM_dd_HH_mm_ss);
		/* 40秒前更新过在线时间的表示在线 */
		String cxtime = DateTimeUtil.addDate(nowtime, DateTimeUtil.yyyy_MM_dd_HH_mm_ss, Calendar.SECOND, -40);
		StringBuffer sql = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		sql.append("\n select count(a.id)");
		sql.append("\n from userInfo a ");
		sql.append("\n where 1=1");
		sql.append("\n and a.lastonlinetime>?");
		args.add(cxtime);
		n = this.countQuery(sql.toString(), args.toArray());
		return n;
	}

	/**
	 * 查询人员信息  人员选择树
	 * @param userInfo
	 * @return 人员信息集合
	 */
	@SuppressWarnings("unchecked")
	public List<UserInfo> listUser(UserInfo userInfo) {
		List<UserInfo> list = null;
//		String nowtime = DateTimeUtil.getNowDateStr(DateTimeUtil.yyyy_MM_dd_HH_mm_ss);
		/* 40秒前更新过在线时间的表示在线 */
//		String cxtime = DateTimeUtil.addDate(nowtime, DateTimeUtil.yyyy_MM_dd_HH_mm_ss, Calendar.SECOND, -40);
		List<Object> args = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append("\n select a.*,b.depname as depName,c.uuid as smImgUuid,c.filename as smImgName,");
		sql.append("\n formedoUser.Id as formedoUserId,formedoUser.Username as formedoUserName ");
		sql.append("\n from userinfo a");
		sql.append("\n inner join userOrganic aa on aa.userid = a.id ");
		sql.append("\n inner join department b on aa.depid = b.id and aa.comId = b.comId ");
		sql.append("\n left join upfiles c on aa.comId = c.comId and aa.smallheadportrait = c.id");
		sql.append("\n left join formedo on a.id=formedo.creator");
		sql.append("\n left join userOrganic formedoOrg on formedo.comId=formedoOrg.Comid and formedo.userid=formedoOrg.Userid and formedoOrg.Enabled=1");
		sql.append("\n left join userinfo formedoUser on formedoOrg.userid=formedoUser.Id");
		sql.append("\n where aa.comId = ? and aa.enabled=?");
		args.add(userInfo.getComId());
		args.add(userInfo.getEnabled());
		if(null!=userInfo.getAnyNameLike()){
			this.addSqlWhereLike(userInfo.getAnyNameLike().toLowerCase(), sql, args, "\n and (lower(a.userName) like ? or lower(a.allSpelling) like ? or lower(a.firstSpelling) like ?)", 3);
		}
		if(userInfo.getAnyDepId() != null){
			sql.append("\n and aa.depid in (");
			sql.append("\n select  a.id from department a where a.comId = ? ");
			args.add(userInfo.getComId());
			sql.append("\n start with a.id =? connect by prior a.id = a.parentid");
			args.add(userInfo.getAnyDepId());
			sql.append("\n )");
		}
		String onlySubState = userInfo.getOnlySubState();
		if(StringUtils.isNotEmpty(onlySubState) && onlySubState.equals("1")){
			sql.append("\n and exists (");
			sql.append("\n select id from myLeaders where creator = a.id and leader = ? and comId = ? and creator <> leader ");
			args.add(userInfo.getId());
			args.add(userInfo.getComId());
			sql.append("\n )");
		}
		sql.append(" order by a.id asc");
		list = this.listQuery(sql.toString(),args.toArray(), UserInfo.class);
		return list;
	}

	/**
	 * 查询用户信息详细  查询详细信息
	 * @param id 所查询的人员表的ID
	 * @return 人员信息
	 */
	public UserInfo getUserInfo(Integer comId,Integer id) {
		StringBuffer sql = new StringBuffer();
		sql.append("\n select a.id,aa.recordCreateTime hireDate,aa.defShowGrpId,case when a.userName is null then a.email else a.userName end as userName,");
		sql.append("\n a.allSpelling,a.firstSpelling,a.password,a.linePhone,a.movePhone,a.selfIntr,");
		sql.append("\n case when uConf.openState is null then 1 else uConf.openState end autoEject,");
		sql.append("\n a.email,a.wechat,a.qq,aa.bigHeadPortrait,aa.mediumHeadPortrait,aa.smallHeadPortrait,a.gender,");
		sql.append("\n a.birthday,a.nickname,aa.comId,aa.job,aa.depId,aa.admin,aa.enabled,aa.lastOnlineTime,aa.isChief,");
		sql.append("\n aa.jifenScore,b.uuid as bigImgUuid,b.filename as bigImgName,e.orgName,");
		sql.append("\n b.uuid as bigImgUuid,b.filename as bigImgName,");
		sql.append("\n c.uuid as midImgUuid,c.filename as midImgName,");
		sql.append("\n d.uuid as smImgUuid,d.filename as smImgName,");
		sql.append("\n f.uuid as logoUuid,f.filename as logoName,g.depName,");
		
		sql.append("\n (select levname from jifenLev where levMinScore=(");
		sql.append("\n select max(levMinScore) from jifenLev where levMinScore<=aa.jifenScore)) as levName,");
		
		sql.append("\n (select levMinScore from jifenLev where levMinScore=(");
		sql.append("\n select min(levMinScore) from jifenLev where levMinScore>aa.jifenScore)) as nextLevJifen, ");
		
		sql.append("\n (select levMinScore from jifenLev where levMinScore=(");
		sql.append("\n select max(levMinScore) from jifenLev where levMinScore<=aa.jifenScore)) as minLevJifen,");
		
		sql.append("\n userLastLogin.recordcreatetime as lastLoginTime,userLoginTimes.times as loginTimes,aa.enrollNumber");
		
		sql.append("\n from  userOrganic aa inner join  userinfo a on aa.userid=a.id");
		sql.append("\n left join userConf uConf on uConf.comId= aa.comId and uConf.userId=aa.userId and uConf.sysConfCode='1' ");
		sql.append("\n left join upfiles b on aa.bigheadportrait=b.id");
		sql.append("\n left join upfiles c on aa.mediumHeadPortrait=c.id");
		sql.append("\n left join upfiles d on aa.smallHeadPortrait=d.id ");
		sql.append("\n left join organic e on aa.comId=e.orgNum ");
		sql.append("\n left join upfiles f on e.logo=f.id ");
		sql.append("\n left join department g on g.id=aa.depId and g.comId=aa.comId ");
		//增加个人上次登录时间以及登录次数查询
		sql.append("\n left join (");
		sql.append("\n select * from (");
		sql.append("\n select a.id,a.recordcreatetime||','||a.content as recordcreatetime,a.comId,a.userid, ROW_NUMBER() OVER (ORDER BY a.id desc) R");
		sql.append("\n from systemLog a where a.comId=? and a.userid=?)");
		sql.append("\n where R = 2 ) userLastLogin on aa.comId=userLastLogin.comId and a.id=userLastLogin.userid");
		sql.append("\n left join (");
		sql.append("\n select a.comId,a.userid,count(*) as times from systemLog a group by a.comId,a.userid");
		sql.append("\n ) userLoginTimes on aa.comId=userLoginTimes.comId and a.id=userLoginTimes.userid");
		sql.append("\n where 1=1 and aa.comId=? and a.id=?");
		return (UserInfo) this.objectQuery(sql.toString(), new Object[] {comId,id,comId,id}, UserInfo.class);
	}

	/**
	 * 验证用户账号唯一性
	 * @param loginName 登录名
	 * @return 账号唯一返回true 
	 */
	public boolean validateLoginName(String loginName) {
		int count = this.countQuery("select count(id) from userinfo where loginName=?", new Object[] { loginName });
		if (count > 0) {
			return false;
		}
		return true;
	}

	/**
	 * 根据账号和密码查找用户
	 * @param loginName 登录名
	 * @param password  密码
	 * @param comId 
	 * @return 人员信息
	 */
	public UserInfo userAuth(String loginName, String password, String comId) {
		StringBuffer sql = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		sql.append("\n select * from ");//人员账号比对
		sql.append("\n (");
//		sql.append("\n select * from");//团队空间允许人数筛选
//		sql.append("\n (");
		sql.append("\n select a.id,aa.recordCreateTime hireDate,case when a.userName is null then a.email else a.userName end as userName,");
		sql.append("\n case when uConf.openState is null then 1 else uConf.openState end autoEject,");
		sql.append("\n a.allSpelling,a.firstSpelling,a.password,a.linePhone,a.movePhone,a.selfIntr,");
		sql.append("\n a.email,a.wechat,a.qq,aa.bigHeadPortrait,aa.mediumHeadPortrait,aa.smallHeadPortrait,a.gender,");
		sql.append("\n a.birthday,a.nickname,aa.comId,aa.job,aa.depId,aa.admin,aa.enabled,aa.lastOnlineTime,aa.ischief,");
		sql.append("\n aa.jifenScore,aa.alterInfo,b.orgName ,h.depName,aa.inService,");
		sql.append("\n e.uuid as smImgUuid,e.filename as smImgName,");
		sql.append("\n g.uuid as logoUuid,g.filename as logoName,");
		sql.append("\n (select levname from jifenLev where levMinScore=(");
		sql.append("\n select max(levMinScore) from jifenLev where levMinScore<=aa.jifenScore)) as levName,");
		sql.append("\n (select levMinScore from jifenLev where levMinScore=(");
		sql.append("\n select min(levMinScore) from jifenLev where levMinScore>aa.jifenScore)) as nextLevJifen,");
		sql.append("\n (select levMinScore from jifenLev where levMinScore=(");
		sql.append("\n select max(levMinScore) from jifenLev where levMinScore<=aa.jifenScore)) as minLevJifen,");
		sql.append("\n case when orgSpace.Usersnum is null then "+ConstantInterface.ORG_DEFAULT_MEMBERS+" else orgSpace.Usersnum end as usersNum");
		sql.append("\n from  userOrganic aa");
		sql.append("\n inner join  userinfo a on aa.userid=a.id");
		sql.append("\n inner join organic b on aa.comId = b.orgNum");
		sql.append("\n left join userConf uConf on uConf.comId= aa.comId and uConf.userId=aa.userId and uConf.sysConfCode='1'");
		sql.append("\n left join upfiles e on aa.smallHeadPortrait=e.id");
		sql.append("\n left join organic f on aa.comId=f.orgNum");
		sql.append("\n left join upfiles g on f.logo=g.id");
		sql.append("\n left join department h on aa.depId=h.id");
		sql.append("\n left join organicSpaceCfg orgSpace on aa.comId = orgSpace.Comid and orgSpace.Enddate >= to_char(sysdate,'yyyy-MM-dd')");
		sql.append("\n where 1=1  and aa.enabled='1' ");
		sql.append("\n and ((b.enabled='0' and aa.admin=1) or  b.enabled='1')  and aa.comId=?");
		args.add(comId);
		sql.append("\n order by hireDate,a.id asc");
//		sql.append("\n ) rowSelect where rownum <=rowSelect.usersNum");
		sql.append("\n ) userSelect where (lower(userSelect.email)=? or lower(userSelect.nickname)=? or userSelect.movephone=?)");
		args.add(loginName);
		args.add(loginName);
		args.add(loginName);
		sql.append("\n and userSelect.password=?");
		args.add(password);
		return (UserInfo) this.objectQuery(sql.toString(), args.toArray(), UserInfo.class);
	}

	/**
	 * 根据部门id批量删除用户信息
	 * @param orgId  用户所属机构ID
	 */
	public void delUserInfoByOrgId(Integer orgId) {
		this.delByField("userinfo", "orgId", orgId);
	}

	/**
	 * 查询指定私有组包含的人员
	 * @param groupId 部门私有组主键id
	 * @return 人员集合
	 */
	@SuppressWarnings("unchecked")
	public List<UserInfo> listUserByGroup(Integer groupId) {
		String nowtime = DateTimeUtil.getNowDateStr(DateTimeUtil.yyyy_MM_dd_HH_mm_ss);
		/* 40秒前更新过在线时间的表示在线 */
		String cxtime = DateTimeUtil.addDate(nowtime, DateTimeUtil.yyyy_MM_dd_HH_mm_ss, Calendar.SECOND, -40);
		StringBuffer sql = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		sql.append("\n select a.id,a.userName ");
		sql.append("\n ,case when a.lastOnlineTime>'" + cxtime + "' then 1 else 0 end ifOnline");
		sql.append("\n from userInfo a");
		sql.append("\n left join userGroupItem b on a.id=b.userid");
		sql.append("\n where b.groupId=?");
		args.add(groupId);
		return this.listQuery(sql.toString(), args.toArray(), UserInfo.class);
	}

	/**
	 * 新企业用户注册主键验证
	 * @param key
	 * @return
	 */
	public boolean orgKeyCheck(Integer key){
		List<Object> args = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer("select * from organic \n");
		sql.append("where orgNum=? \n");
		args.add(key);
		Organic organic = (Organic)this.objectQuery(sql.toString(), args.toArray(),Organic.class);
		if(null!=organic){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * 修改密码前验证密码
	 * @param id
	 * @param passwordMD5
	 * @return
	 */
	public boolean validatePassword(String id, String passwordMD5) {
		StringBuffer sql = new StringBuffer("select * from userinfo \n");
		sql.append("\n where id=? and password=?");
		//查询有结果则密码正确，否则密码错误
		return this.objectQuery(sql.toString(), new Object[]{id,passwordMD5}, UserInfo.class)==null?false:true;
	}

	/**
	 * 查询部门的用户列表
	 * @param userInfo
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserInfo> listPagedUserForDep(UserInfo userInfo) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append("\n select a.id,case when a.userName is null then a.email else a.userName end as userName,");
		sql.append("\n a.allSpelling,a.firstSpelling,a.password,a.linePhone,a.movePhone,");
		sql.append("\n a.email,a.wechat,a.qq,aa.bigHeadPortrait,aa.mediumHeadPortrait,aa.smallHeadPortrait,a.gender,");
		sql.append("\n a.birthday,a.nickname,aa.comId,aa.job,aa.depId,aa.admin,aa.enabled,aa.lastOnlineTime,");
		sql.append("\n aa.jifenScore,b.depname, ");
		sql.append("\n case when aa.depid="+userInfo.getDepId()+" then 0 else 1 end as neworder");
		sql.append("\n from userinfo a inner join userOrganic aa on a.id=aa.userId");
		sql.append("\n left join department b on aa.depid=b.id and aa.comId=b.comId");
		sql.append("\n where 1=1 ");
		this.addSqlWhere(userInfo.getComId(), sql, args, " and aa.comId=?");
		if(null!=userInfo.getCondition()){
			this.addSqlWhereLike(userInfo.getCondition().toLowerCase(), sql, args, " and (lower(a.email) like ? or lower(a.userName) like ? or lower(a.allSpelling) like ? or lower(a.firstSpelling) like ?)",4);
		}
		return this.pagedQuery(sql.toString(), " neworder,aa.depid,aa.enabled desc,a.id", args.toArray(), UserInfo.class);
	}
	/**
	 * 查询分组的用户列表
	 * @param userInfo
	 * @param grpId 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserInfo> listPagedUserForGrp(UserInfo userInfo, String grpId) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer sqlCount = new StringBuffer("select count(*) from (");
		
		StringBuffer sql = new StringBuffer();
		sql.append("\n select a.id,case when a.userName is null then a.email else a.userName end as userName,");
		sql.append("\n a.allSpelling,a.firstSpelling,a.password,a.linePhone,a.movePhone,f.uuid as smImgUuid,f.filename as smImgName,");
		sql.append("\n a.email,a.wechat,a.qq,aa.bigHeadPortrait,aa.mediumHeadPortrait,aa.smallHeadPortrait,a.gender,");
		sql.append("\n a.birthday,a.nickname,aa.comId,aa.job,aa.depId,aa.admin,aa.enabled,aa.lastOnlineTime,");
		sql.append("\n aa.jifenScore,c.depName, ");
		sql.append("\n case when a.id in (select b.userinfoid from groupPersion b where b.grpid="+grpId+") then  ");
		sql.append("\n case when a.id = "+userInfo.getId()+" then 1 else 0 end else ");
		sql.append("\n case when a.id = "+userInfo.getId()+" then 1 else 2 end end as neworder");
		sql.append("\n from userinfo a");
		sql.append("\n inner join userOrganic aa on a.id=aa.userId and aa.enabled =1");
		sql.append("\n left join department c on aa.depid=c.id and aa.comId=c.comId");
		sql.append("\n left join upfiles f on aa.comId = f.comId and aa.smallheadportrait = f.id");
		sql.append("\n where 1=1 and aa.enabled='1'");
		this.addSqlWhere(userInfo.getComId(), sql, args, " and aa.comId=?");
		if(null!=userInfo.getCondition()){
			this.addSqlWhereLike(userInfo.getCondition().toLowerCase(), sql, args, " and (lower(a.email) like ? or lower(a.userName) like ? or lower(a.allSpelling) like ? or lower(a.firstSpelling) like ?)",4);
		}
		sqlCount.append(sql);
		sqlCount.append(" )a where 1=1");
		return this.pagedQuery(sql.toString(), sqlCount.toString()," neworder,aa.depid,a.id", args.toArray(), UserInfo.class);
	}

	/**
	 * 分页查询部门的成员
	 * @param depId 部门主键
	 * @param comId 企业编号
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserInfo> listPagedDepUser(Integer depId, Integer comId) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append("\n select a.id,case when a.userName is null then a.email else a.userName end as userName,");
		sql.append("\n a.allSpelling,a.firstSpelling,a.password,a.linePhone,a.movePhone,");
		sql.append("\n a.email,a.wechat,a.qq,aa.bigHeadPortrait,aa.mediumHeadPortrait,aa.smallHeadPortrait,a.gender,");
		sql.append("\n a.birthday,a.nickname,aa.comId,aa.job,aa.depId,aa.admin,aa.enabled,aa.lastOnlineTime,");
		sql.append("\n aa.jifenScore");
		sql.append("\n from userinfo a inner join userOrganic aa on a.id=aa.userId");
		sql.append("\n where 1=1 and aa.enabled='1'");
		this.addSqlWhere(comId, sql, args, " and aa.comId=?");
		this.addSqlWhere(depId, sql, args, " and aa.depid=?");
		return this.pagedQuery(sql.toString(), " a.id", args.toArray(), UserInfo.class);
	}
	/**
	 * 部门的成员
	 * @param depId 部门主键
	 * @param comId 企业编号
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserInfo> listDepUser(Integer depId, Integer comId) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append("\n select a.id,case when a.userName is null then a.email else a.userName end as userName,");
		sql.append("\n a.allSpelling,a.firstSpelling,a.password,a.linePhone,a.movePhone,");
		sql.append("\n a.email,a.wechat,a.qq,aa.bigHeadPortrait,aa.mediumHeadPortrait,aa.smallHeadPortrait,a.gender,");
		sql.append("\n a.birthday,a.nickname,aa.comId,aa.job,aa.depId,aa.admin,aa.enabled,aa.lastOnlineTime,aa.enabled,");
		sql.append("\n aa.jifenScore");
		sql.append("\n from userinfo a inner join userOrganic aa on a.id=aa.userId");
		sql.append("\n where 1=1 ");
		this.addSqlWhere(comId, sql, args, " and aa.comId=?");
		this.addSqlWhere(depId, sql, args, " and aa.depid=?");
		sql.append("\n order by aa.enabled desc,a.id asc");
		
		return this.listQuery(sql.toString(), args.toArray(), UserInfo.class);
	}
	/**
	 * 取得在职用户信息
	 * @param depId
	 * @param comId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserInfo> listEnabledUser(Integer depId, Integer comId) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append("\n select a.id,case when a.userName is null then a.email else a.userName end as userName,");
		sql.append("\n a.allSpelling,a.firstSpelling,a.password,a.linePhone,a.movePhone,");
		sql.append("\n a.email,a.wechat,a.qq,aa.bigHeadPortrait,aa.mediumHeadPortrait,aa.smallHeadPortrait,a.gender,");
		sql.append("\n a.birthday,a.nickname,aa.comId,aa.job,aa.depId,aa.admin,aa.enabled,aa.lastOnlineTime,aa.enabled,");
		sql.append("\n aa.jifenScore");
		sql.append("\n from userinfo a inner join userOrganic aa on a.id=aa.userId");
		sql.append("\n where aa.enabled=1 ");
		this.addSqlWhere(comId, sql, args, " and aa.comId=?");
		this.addSqlWhere(depId, sql, args, " and aa.depid=?");
		sql.append("\n order by aa.enabled desc,a.id asc");
		
		return this.listQuery(sql.toString(), args.toArray(), UserInfo.class);
	}

	
	
	/**
	 * 验证当前邮箱是否设置密码，用于统一密码
	 * @param account
	 * @return
	 */
	public UserInfo getUserInfoByAccount(String account) {
		StringBuffer sql = new StringBuffer("select * from (");
		sql.append("\n select rownum, a.* from userinfo a ");
		sql.append("\n where (lower(a.email)=? or lower(a.nickname)=? or a.movephone=?) order by id asc");
		sql.append("\n ) a where rownum=1");
		return (UserInfo) this.objectQuery(sql.toString(), new Object[]{account,account,account}, UserInfo.class);
	}

	
	
	/**
	 *  取得公司的唯一账号
	 * @param account
	 * @param comId
	 * @return
	 */
	public UserInfo getUserInfoByType(String account, Integer comId,String type) {
		List<Object> args = new ArrayList<Object>();
		
		StringBuffer sql = new StringBuffer("select * from (");
		sql.append("\n select a.*,b.comId,b.enabled from userinfo a inner join userorganic b on a.id=b.userid");
		sql.append("\n where 1=1");
		this.addSqlWhere(comId, sql, args, " and  b.comId=?");
		if(type.equals(ConstantInterface.GET_BY_EMAIL)){
			this.addSqlWhere(account, sql, args, " and lower(a.email)=?");
		}else if(type.equals(ConstantInterface.GET_BY_PHONE)){
			this.addSqlWhere(account, sql, args, " and lower(a.MOVEPHONE)=?");
		}
		sql.append("\n ) a where rownum=1");
		
		return (UserInfo) this.objectQuery(sql.toString(), args.toArray(), UserInfo.class);
	}

	/**
	 * 修改密码
	 * @param userInfo
	 */
	public void updatePassword(UserInfo userInfo) {
		StringBuffer sql = new StringBuffer();
		sql.append("\n update userinfo set password=:password where id=:id");
		this.update(sql.toString(),userInfo);
		
	}



	/**
	 * 职位变动
	 * @param comId 企业编号
	 * @param userId用户主键
	 * @param job 职位
	 */
	public void updateUserOrganic(Integer comId, Integer userId, String job) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append(" update userOrganic set job=? where comId=? and  userId=?");
		args.add(job);
		args.add(comId);
		args.add(userId);
		this.excuteSql(sql.toString(), args.toArray());
	}

	/**
	 * 验证用户登陆信息
	 * @param email
	 * @param passwordMD5
	 * @return
	 */
	public UserInfo checkUserInfo(String email, String passwordMD5) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer("select * from (");
		sql.append("select rownum,a.* from userinfo a where 1=1 ");
		this.addSqlWhere(email, sql, args, " and lower(a.email)=? ");
		this.addSqlWhere(passwordMD5, sql, args, " and a.password=?");
		sql.append("\n ) a where rownum=1");
		return (UserInfo) this.objectQuery(sql.toString(), args.toArray(), UserInfo.class);
	}

	/**
	 * 修改头像
	 * @param comId 企业编号
	 * @param userId 用户主键
	 * @param bigHeadPortrait 大头像
	 * @param mediumHeadPortrait 中头像
	 * @param smallHeadPortrait 小头像
	 */
	public void updateUserOrganicImg(Integer comId, Integer userId,
			Integer bigHeadPortrait, Integer mediumHeadPortrait,
			Integer smallHeadPortrait) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append(" update userOrganic set bigHeadPortrait=?,mediumHeadPortrait=?,smallHeadPortrait=? where comId=? and  userId=?");
		args.add(bigHeadPortrait);
		args.add(mediumHeadPortrait);
		args.add(smallHeadPortrait);
		args.add(comId);
		args.add(userId);
		this.excuteSql(sql.toString(), args.toArray());
		
	}
	

	/**
	 * 通过帐号修改用户
	 * @param user
	 */
	public void updateUserInfoByAccount(UserInfo user) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append(" update userinfo set password=? where (lower(email)=? or movePhone=?)");
		args.add(user.getPassword());
		args.add(StringUtil.isBlank(user.getEmail())?"":user.getEmail().toLowerCase());
		args.add(StringUtil.isBlank(user.getMovePhone())?"":user.getMovePhone());
		this.excuteSql(sql.toString(), args.toArray());
		
	}

	/**
	 * 树形查询部门的用户列表
	 * @param userInfo
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserInfo> listPagedUserTreeForDep(UserInfo userInfo) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append("\n select a.*,b.depname as depName,c.uuid as smImgUuid,c.filename as smImgName,aa.enabled from userinfo a");
		sql.append("\n inner join userOrganic aa on aa.userid = a.id ");
		sql.append("\n inner join department b on aa.depid = b.id and aa.comId = b.comId ");
		sql.append("\n left join upfiles c on aa.comId = c.comId and aa.smallheadportrait = c.id");
		sql.append("\n where 1=1");
		this.addSqlWhere(userInfo.getComId(), sql, args, " and aa.comId = ?");
		if(userInfo.getDepId() != null && userInfo.getDepId()>-1){
			sql.append("\n and aa.depid in (");
			sql.append("\n select  a.id from department a where a.comId = ?");
			args.add(userInfo.getComId());
			sql.append("\n start with a.id =? connect by prior a.id = a.parentid");
			args.add(userInfo.getDepId());
			sql.append("\n )");
		}
		if(null!=userInfo.getCondition()){
			this.addSqlWhereLike(userInfo.getCondition().toLowerCase(), sql, args, " and (lower(a.email) like ? or lower(a.userName) like ? or lower(a.allSpelling) like ? or lower(a.firstSpelling) like ?)",4);
		}
		return this.pagedQuery(sql.toString()," aa.enabled desc,aa.depid,a.id desc",args.toArray(),UserInfo.class);
	}

	/**
	 * 公司总人数
	 * @param comId
	 * @return
	 */
	public Integer countUsers(Integer comId) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append(" select count(a.id) from userorganic a where a.enabled=1 ");
		this.addSqlWhere(comId, sql, args, " and a.comId=?");
		return this.countQuery(sql.toString(), args.toArray());
	}

	/**
	 * 验证当前的用户的登录别名
	 * @param nickName
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserInfo> checkNickName(String nickName, Integer userId) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append("\n select a.id from userInfo a where 1=1 ");
		this.addSqlWhere(nickName, sql, args, " and lower(a.nickName)=? ");
		this.addSqlWhere(userId, sql, args, " and a.id<>? ");
		return this.listQuery(sql.toString(), args.toArray(), UserInfo.class);
	}

	/**
	 * 修改登录别名
	 * @param userInfo
	 */
	public void updateUserNickname(UserInfo userInfo) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append(" update userInfo set nickname=? where id=?");
		args.add(userInfo.getNickname());
		args.add(userInfo.getId());
		this.excuteSql(sql.toString(), args.toArray());
		
	}

	/**
	 * 修改用户名
	 * @param userInfo
	 */
	public void updateUserName(UserInfo userInfo) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		if(!"".equals(StringUtil.delNull(userInfo.getUserName()))){
			sql.append(" update userInfo set userName=?,allSpelling=?,firstSpelling=? where id=?");
			args.add(userInfo.getUserName());
			args.add(userInfo.getAllSpelling());
			args.add(userInfo.getFirstSpelling());
			args.add(userInfo.getId());
			this.excuteSql(sql.toString(), args.toArray());
		}else{
			sql.append(" update userInfo set userName=? where id=?");
			args.add(userInfo.getUserName());
			args.add(userInfo.getId());
			this.excuteSql(sql.toString(), args.toArray());
		}
		
	}

	/**
	 * 修改用户性别
	 * @param userInfo
	 */
	public void updateUserGender(UserInfo userInfo) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append(" update userInfo set gender=? where id=?");
		args.add(userInfo.getGender());
		args.add(userInfo.getId());
		this.excuteSql(sql.toString(), args.toArray());
	}

	/**
	 * 修改用户生日
	 * @param userInfo
	 */
	public void updateUserBirthday(UserInfo userInfo) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append(" update userInfo set birthday=? where id=?");
		args.add(userInfo.getBirthday());
		args.add(userInfo.getId());
		this.excuteSql(sql.toString(), args.toArray());
	}

	/**
	 * 修改用户QQ
	 * @param userInfo
	 */
	public void updateUserQq(UserInfo userInfo) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append(" update userInfo set qq=? where id=?");
		args.add(userInfo.getQq());
		args.add(userInfo.getId());
		this.excuteSql(sql.toString(), args.toArray());
		
	}
	/**
	 * 修改用户手机号
	 * @param userInfo
	 */
	public void updateUserMovePhone(UserInfo userInfo) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append(" update userInfo set movePhone=? where id=?");
		args.add(userInfo.getMovePhone());
		args.add(userInfo.getId());
		this.excuteSql(sql.toString(), args.toArray());
		
	}

	/**
	 * 修改用户座机号
	 * @param userInfo
	 */
	public void updateUserLinePhone(UserInfo userInfo) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append(" update userInfo set linePhone=? where id=?");
		args.add(userInfo.getLinePhone());
		args.add(userInfo.getId());
		this.excuteSql(sql.toString(), args.toArray());
		
	}

	/**
	 * 修改用户微信号
	 * @param userInfo
	 */
	public void updateUserWechat(UserInfo userInfo) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append(" update userInfo set wechat=? where id=?");
		args.add(userInfo.getWechat());
		args.add(userInfo.getId());
		this.excuteSql(sql.toString(), args.toArray());
		
	}

	

	/**
	 * 企业的所有成员主键(在岗的)
	 * @param comId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserInfo> listUser(Integer comId) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append("\n select b.id,b.email,b.wechat,b.qq,b.userName,a.comId,a.enrollNumber from userorganic a ");
		sql.append("\n inner join userinfo b on a.userid=b.id where a.enabled=1 and a.comId=?");
		args.add(comId);
		return this.listQuery(sql.toString(), args.toArray(), UserInfo.class);
	}
	/**
	 * 企业的所有成员主键，打卡编号(在岗的)
	 * @param comId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserInfo> listUserWithEnNumber(Integer comId) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append("\n select b.id,b.email,b.wechat,b.qq,b.userName,a.comId,a.enrollNumber from userorganic a ");
		sql.append("\n inner join userinfo b on a.userid=b.id where a.enabled=1 and a.comId=? and a.enrollNumber is not null");
		args.add(comId);
		return this.listQuery(sql.toString(), args.toArray(), UserInfo.class);
	}
	

	/**
	 *  用户头像信息
	 * @param comId 企业号
	 * @param userId 用户主键
	 * @return
	 */
	public UserInfo getUserHeadImg(Integer comId, Integer userId) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append("\n select a.comId,a.bigHeadPortrait,a.mediumHeadPortrait,a.smallHeadPortrait from userOrganic a");
		sql.append("\n where a.comId = ? and a.userId=?");
		args.add(comId);
		args.add(userId);
		return (UserInfo) this.objectQuery(sql.toString(), args.toArray(), UserInfo.class);
	}

	/**
	 * 取得企业的管理人员
	 * @param comId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserInfo> listOrgAdmin(Integer comId) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer("");
		sql.append("\n select b.id,b.email,b.wechat,b.qq,b.userName from userorganic a ");
		sql.append("\n inner join userinfo b on a.userid=b.id ");
		sql.append("\n where a.enabled=1 and a.admin>0 ");
		this.addSqlWhere(comId, sql, args, " and a.comId=?");
		return this.listQuery(sql.toString(), args.toArray(), UserInfo.class);
	}

	

}
