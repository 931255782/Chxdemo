package com.yc.blog;



import java.util.List;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yc.blog.bean.Article;
import com.yc.blog.bean.ArticleExample;
import com.yc.blog.bean.ArticleExample.Criteria;
import com.yc.blog.biz.MailService;
import com.yc.blog.dao.ArticleMapper;
import com.yc.blog.dao.UserMapper;

@SpringBootTest
class BlogApplicationTests {
		
	@Resource
	private UserMapper um;

	
	@Resource
	private ArticleMapper am;
	
	@Resource
	MailService ms;
	@Test
	void contextLoads() {

		Assert.isTrue(um.selectByExample(null).size()>0,"结果集数量不正确!");
		ArticleExample ae=new ArticleExample();
		Criteria c=ae.createCriteria();
		c.andCreatetimeIsNull();
		c.andTitleLike("%css%");
//		Criteria c1=ae.createCriteria();
//		c1.andLabelLike("%s%").andCategoryidBetween(0, 2);
		List<Article> list=am.selectByExample(ae);
		System.out.println(list);
	}
	
	@Test
	void test1() {
		Page<Article> page=  PageHelper.startPage(1, 5);
		am.selectByExample(null);
		
		
//		for (Article article : list) {
//			System.out.println(article.getTitle());
//		}
		
		for (Article article : page) {
			System.out.println(article.getTitle());
		}
	}
	
	
	@Test
	public void test2() {
		ms.sendSimpleMail("931255782@qq.com", "测试邮件", "金佛我回复了卡号fair阿克洛夫南卡江");
	}
}
