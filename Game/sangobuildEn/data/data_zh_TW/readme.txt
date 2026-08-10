生成繁体版本的过程

下面是必须手工完成的部分。
1. 打开编辑器，选择“国际化”->“处理项目数据”。这一步会把data目录下的内容复制到data_zh_TW/data目录下，并完成简繁转换。如果有非文字内容需要特殊处理，那么需要在做这一步之前把特殊繁体图片内容放到data_zh_TW/special_resources目录下。
2. 编辑器中，选择“国际化”->“处理源代码(仅提取)”，选择Sango1.0-Server/src目录进行处理。
3. 打开data_zh_TW/messages.xls，查看是否有新发现的未翻译文字（列在一个新的工作表里）。如果有，需要做翻译处理。

下面是可以自动完成的步骤（需保证所有文字资源都翻译完成，保存到data_zh_TW/messages.xls里）。
1. 处理data目录，伪代码如下：
	ProjectData proj = new ProjectData();
	proj.load(data目录对应的路径);
	List<LocaleConfig> locales = LocaleConfig.getLocales(proj);
	I18NProcessor proc = new I18NProcessor(proj, locales.get(0));
	proc.process(true);
2. 处理服务器源代码，伪代码如下：
	ProjectData proj = new ProjectData();
	proj.load(data目录对应的路径);
	List<LocaleConfig> locales = LocaleConfig.getLocales(proj);
	I18NProcessor proc = new I18NProcessor(服务器源代码目录, locales.get(0));
3. 生成繁体版本的技能和buff类，伪代码如下：
	ProjectData proj = new ProjectData();
	proj.load(data_zh_TW/data目录);
	proj.generateBuffClasses();
	proj.generateSkillClasses();
4. 提交data_zh_TW/data目录到CVS。
5. 繁体版本做版本号，生成client_pkg。伪代码如下：
	ProjectData proj = new ProjectData();
	proj.load(data_zh_TW/data目录);
	proj.generateResourceVersionXML();
6. 再次提交data_zh_TW/data目录到CVS。
7. 编译，生成安装包。

客户端打包时，需要使用data_zh_TW/data/client_pkg目录下的资源。