package com.hsjfans.github.generator;


import com.hsjfans.github.config.Config;
import com.hsjfans.github.model.ApiTree;
import com.hsjfans.github.model.ControllerClass;
import com.hsjfans.github.model.ControllerMethod;
import com.hsjfans.github.model.RequestParam;
import com.hsjfans.github.util.CollectionUtil;
import com.hsjfans.github.util.FileUtil;
import com.hsjfans.github.util.StringUtil;


/**
 *
 * html 文档生成器
 *
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public class HtmlGenerator extends AbstractGenerator {


    private static final String BASE_TPL_PATH = "src/main/resources/tpl/";
    private static final String controllerTpl = "api-controller.html";
    private static final String Index = "index.html";
    private static final String urlTpl = "api-url.html";
    private static final String extraTpl = "js.html";


    @Override
    public void from(ApiTree apiTree, Config config) {
        this.config = config;
        buildExtraDoc();
        StringBuilder controllerList = new StringBuilder();
        apiTree.getSet().forEach(
                controllerClass -> {
                    controllerList.append(String.format("\n <li style=\"padding:5px\" > <a href=\"%s\" >%s</a> <span> %s </span>  </li>"
                    ,"./"+controllerClass.getName()+".html",controllerClass.getName(),controllerClass.getDescription()));
                    buildControllerDoc(controllerClass);
                }
        );
        String indexHtml = FileUtil.from(BASE_TPL_PATH+Index);
        indexHtml =  indexHtml.replace("${api-doc-description}",config.getDocName());
        indexHtml =  indexHtml.replace("${api-doc-name}",config.getDocName());
        indexHtml = indexHtml.replace("${api-controller-item}",controllerList.toString());
        indexHtml = indexHtml.replace("${count}",String.valueOf(apiTree.getSet().size()));
        FileUtil.to(this.config.getOutPath()+"index.html",indexHtml);

    }


    @Override
    protected void buildControllerDoc(ControllerClass controllerClass) {

        StringBuilder controllerHtml = new StringBuilder();
        String controller = FileUtil.from(BASE_TPL_PATH+controllerTpl);
//        controller =  controller.replace("${api-url-description}",controllerClass.getName());
        controller =  controller.replace("${controller-name}",controllerClass.getName());
        controller =  controller.replace("${count}",String.valueOf(controllerClass.getControllerMethod().size()));
        controller =  controller.replace("${controller-description}",controllerClass.getDescription());
        controller =  controller.replace("${author}",controllerClass.getAuthor());
        controller =  controller.replace("${baseUrl}", StringUtil.join(controllerClass.getUrl(),","));
        controllerClass.getControllerMethod().forEach(controllerMethod -> {
            controllerHtml.append(String.format("\n <li style=\"padding:5px\"> <a href=\"%s\" >%s</a> <span> %s </span> </li>"
                    ,"./"+controllerClass.getName()+"_"+controllerMethod.getName()+".html",controllerMethod.getName(),controllerMethod.getName()));
            buildApiDoc(controllerClass,controllerMethod);
        });
        controller =  controller.replace("${controller-methods}",controllerHtml.toString());
        FileUtil.to(this.config.getOutPath()+controllerClass.getName()+".html",controller);
    }

    @Override
    protected void buildApiDoc(ControllerClass controllerClass, ControllerMethod controllerMethod) {

        String method = FileUtil.from(BASE_TPL_PATH+urlTpl);
        method = method.replace("${title}",controllerMethod.getName());
        method = method.replace("${api-url-name}",controllerMethod.getName());
        method = method.replace("${prev-name}",controllerClass.getName());
        method = method.replace("${prev-url}",controllerClass.getName()+".html");
        if(controllerClass.getUrl().length==0){
            controllerClass.setUrl(new String[]{""});
        }
        if(controllerMethod.getUrl().length==0){
            controllerMethod.setUrl(new String[]{""});
        }
        String[] urls = new String[controllerClass.getUrl().length*controllerMethod.getUrl().length];
        int i=0;
        for(String baseUrl:controllerClass.getUrl()){
            for(String url:controllerMethod.getUrl()){
               urls[i++] = baseUrl+url;
            }
        }
        method = method.replace("${urls}",StringUtil.join(urls,","));
        method = method.replace("${api-url-description}",controllerMethod.getDescription());
        method = method.replace("${methods}", CollectionUtil.requestMethodsToString(controllerMethod.getMethods()));
        method = method.replace("${author}",controllerMethod.getAuthor());

        StringBuilder params = new StringBuilder();
        controllerMethod.getParams().forEach(requestParam->{
            if(requestParam.getParams()==null){
                params.append(String.format(" \n <tr>\n" +
                        "            <td>%s</td>\n" +
                        "            <td>%s</td>\n" +
                        "            <td>%s</td>\n" +
                        "            <td>%s</td>\n" +
                        "            <td>%s</td>\n" +
                        "            <td>%s</td>\n" +
                        "        </tr> ",requestParam.getName(),requestParam.getType(),
                        StringUtil.enumToStrs(requestParam.getEnumValues()),requestParam.isNecessary(),requestParam.isFuzzy(),
                        requestParam.getDescription()));
            }else {
                // todo
            }
        });
        method = method.replace("${requestParams}", params.toString());

        StringBuilder responses = new StringBuilder();
        responses.append(String.format(
                " <tr>\n" +
                        "            <td>%s</td>\n" +
                        "            <td>%s</td>\n" +
                        "            <td>%s</td>\n" +
                        "        </tr>"
        ,controllerMethod.getResponseReturn().getName(),
                controllerMethod.getResponseReturn().getType(),
                controllerMethod.getResponseReturn().getDescription()));

        method = method.replace("${responses}", responses.toString());


        FileUtil.to(this.config.getOutPath()+controllerClass.getName()+"_"+controllerMethod.getName()+".html",method);
    }

    @Override
    protected void buildExtraDoc() {
        String extra = FileUtil.from(BASE_TPL_PATH+extraTpl);
        FileUtil.to(this.config.getOutPath()+extraTpl,extra);
    }
}
