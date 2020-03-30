package com.yc.blog.web;

import java.io.File;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yc.blog.bean.Article;
import com.yc.blog.bean.ArticleExample;
import com.yc.blog.bean.Category;
import com.yc.blog.bean.User;
import com.yc.blog.bean.UserExample;
import com.yc.blog.biz.BizException;
import com.yc.blog.biz.UserBiz;
import com.yc.blog.bean.ArticleExample.Criteria;
import com.yc.blog.dao.ArticleMapper;
import com.yc.blog.dao.CategoryMapper;
import com.yc.blog.dao.UserMapper;
import com.yc.blog.vo.Result;

@Controller
public class IndexAction {

	@Resource
	private ArticleMapper am;

	@Resource
	private CategoryMapper cm;

	@Resource
	private UserBiz ubiz;

	@Resource
	private UserMapper um;

	@GetMapping({ "/", "index", "index.html" })
	public String index(@RequestParam(defaultValue = "1") Integer page, Model model) {
		Page<Article> pg = PageHelper.startPage(page, 5);
		// PageHelper.startPage必须在查询方法前一行代码执行

		ArticleExample ae = new ArticleExample();
		ae.setOrderByClause("createtime desc");
		am.selectByExampleWithBLOBs(ae);
		model.addAttribute("alist", pg);

		return "index";
	}

	@GetMapping({ "article" })
	public String article(Integer id, Model model) {
		Article a = am.selectByPrimaryKey(id);
		model.addAttribute(a);
		return "article";
	}

	@GetMapping({ "category" })
	public String category(@RequestParam(defaultValue = "1") Integer page, Integer id, Model model) {
		Page<Article> pg = PageHelper.startPage(page, 5);
		ArticleExample ae = new ArticleExample();
		ae.createCriteria().andCategoryidEqualTo(id);
		am.selectByExample(ae);
		model.addAttribute("alist", pg);
		model.addAttribute("id", id);
		return "category";
	}

	@ModelAttribute
	public void common(Model model) {
		List<Category> c = cm.selectByExample(null);
		model.addAttribute("clist", c);

		ArticleExample ae = new ArticleExample();
		Criteria cr = ae.createCriteria();
		cr.andReadcntGreaterThan(100);
		List<Article> list = am.selectByExample(ae);
		model.addAttribute("list", list);
	}

	@GetMapping("toreg")
	public String reg() {
		return "reg";
	}

	@Value("${myUploadPath}")
	private String myUploadPath;

	/*
	 * Ajax 要使用@ResponseBody注解
	 */
	@PostMapping("reg")
	@ResponseBody
	public Result toreg(@Valid User user, Errors errors, @RequestParam("file") MultipartFile file, String repwd)
			throws IllegalStateException, IOException {
		if (errors.hasFieldErrors()) {
			return new Result(1, "用户注册失败", errors.getFieldErrors());
		}

		file.transferTo(new File(myUploadPath + file.getOriginalFilename()));

		try {
			String head = "/" + file.getOriginalFilename();

			user.setHead(head);
			ubiz.reg(user, repwd);
			return new Result(0, "用户注册成功");
		} catch (BizException e) {

			e.printStackTrace();

			errors.rejectValue(e.getName(), "" + e.getCode(), e.getMessage());
			return new Result(e.getCode(), "用户注册失败", errors.getFieldErrors());
		}
	}

	@PostMapping("login")
	@ResponseBody
	public Result login(@Valid User user, Errors errors, HttpSession session) {
		if (errors.hasFieldErrors("account") || errors.hasFieldErrors("pwd")) {
			return new Result(1, "请输入用户名和密码");
		}

		try {
			User dbuser = ubiz.login(user);
			session.setAttribute("loginedUser", dbuser);
			return new Result(0, "登入成功", dbuser);
		} catch (BizException e) {
			e.printStackTrace();
			return new Result(e.getCode(), e.getMessage());
		}
	}

	@GetMapping("editArticle")
	public String toEditArticle() {
		return "article_edit";
	}

	@PostMapping("saveArticle")
	public String saveArticle(Article a, @SessionAttribute("loginedUser") User user) {
		a.setAuthor(user.getName());
		a.setCreatetime(new Date());
		am.insert(a);
		return "redirect:article?id=" + a.getId();
	}

	@GetMapping("toforget")
	public String forget() {
		return "forget";
	}

	@PostMapping("sendVcode")
	@ResponseBody
	public Result sendVcode(String account, HttpSession session) {
		try {
			String vcode = ubiz.forget(account);
			session.setAttribute("vcode", vcode);
			
			return new Result(0, "验证码发送成功");
		} catch (BizException e) {
			e.printStackTrace();
			return new Result(1, e.getMessage());
		} catch (RuntimeException e) {
			e.printStackTrace();
			return new Result(1, "邮件发送失败，请联系客服");
		}
	}

	@PostMapping("changePwd")
	@ResponseBody
	public Result changePwd(User user, String repwd, String vcode, 
			@SessionAttribute("vcode") String sessionVcode) {
		System.out.println(vcode + "======" + sessionVcode);

		UserExample ue = new UserExample();
		ue.createCriteria().andPwdEqualTo(user.getPwd());
		um.updateByExampleSelective(user, ue);


		if (sessionVcode.equals(vcode) == false) {
			return new Result(0, "验证码输入错误");
		} else {
			return new Result(0, "验证码输入正确");
		}

	}

}
