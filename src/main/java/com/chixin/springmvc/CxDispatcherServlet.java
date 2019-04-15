package com.chixin.springmvc;

import com.alibaba.fastjson.JSON;
import com.chixin.Util.ClassUtil;
import com.chixin.note.CxControl;
import com.chixin.note.CxRequestMapping;
import com.chixin.note.CxResponseBody;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CxDispatcherServlet extends HttpServlet {

    // springmvc 容器对象 key:类名id ,value 对象
    private ConcurrentHashMap<String, Object> springmvcBeans = new ConcurrentHashMap<String, Object>();
    // springmvc 容器对象 keya:请求地址 ,vlue类
    private ConcurrentHashMap<String, Object> urlBeans = new ConcurrentHashMap<String, Object>();
    // springmvc 容器对象 key:请求地址 ,value 方法对象
    private ConcurrentHashMap<String, Method> urlMethods = new ConcurrentHashMap<String, Method>();

    @Override
    public void init() throws ServletException {
        // 1.获取当前包下的所有的类
        List<Class<?>> classes = ClassUtil.getClasses("com.chixin");
        // 2.将扫包范围所有的类,注入到springmvc容器里面，存放在Map集合中 key为默认类名小写，value 对象
        try {
            findClassMVCAnnotation(classes);
        } catch (Exception e) {
            // TODO: handle exception
        }
        // 3.将url映射和方法进行关联
        handlerMapping();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // #################处理请求####################
        // 1.获取请求url地址
        String requestURI = req.getRequestURI().substring(req.getContextPath().length());
        if (StringUtils.isEmpty(requestURI)) {
            return;
        }
        // 2.从Map集合中获取控制对象
        Object object = urlBeans.get(requestURI);
        if (object == null) {
            resp.getWriter().println(" not found 404  url");
            return;
        }
        // 3.使用url地址获取方法
        Method method = urlMethods.get(requestURI);
        if (method==null) {
            resp.getWriter().println(" not found method");
        }
        // 4.使用java的反射机制调用方法
        Object result = methodInvoke(object, method);
        // 5.查看有没有ResponseBody注解
        if(method.getDeclaredAnnotation(CxResponseBody.class)!=null||object.getClass().getDeclaredAnnotation(CxResponseBody.class)!=null){
            //解析成字符串返回
            resp.getWriter().write(JSON.toJSONString(result));
        }else{
            //调用视图转换器渲染给页面展示
            extResourceViewResolver((String)result, req, resp);
        }


    }

    private void extResourceViewResolver(String pageName, HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        // 根路径
        String prefix = "/";
        String suffix = ".jsp";
        req.getRequestDispatcher(prefix + pageName + suffix).forward(req, res);
    }

    private Object methodInvoke(Object object, Method method) {
        try {
            Object result = method.invoke(object);
            return result;
        } catch (Exception e) {
            return null;
        }

    }

    // 2.将扫包范围所有的类,注入到springmvc容器里面，存放在Map集合中 key为默认类名小写，value 对象
    public void findClassMVCAnnotation(List<Class<?>> classes)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        for (Class<?> classInfo : classes) {
            // 判断类上是否有加上注解
            CxControl extController = classInfo.getDeclaredAnnotation(CxControl.class);
            if (extController != null) {
                // 默认类名是小写
                String beanId = ClassUtil.toLowerCaseFirstOne(classInfo.getSimpleName());
                // 实例化对象
                Object object = ClassUtil.newInstance(classInfo);
                springmvcBeans.put(beanId, object);
            }
        }
    }

    // 3.将url映射和方法进行关联
    public void handlerMapping() {
        // 1.遍历springmvc bean容器 判断类上属否有url映射注解
        for (Map.Entry<String, Object> mvcBean : springmvcBeans.entrySet()) {
            // 2.遍历所有的方法上是否有url映射注解
            // 获取bean的对象
            Object object = mvcBean.getValue();
            // 3.判断类上是否有加url映射注解
            Class<? extends Object> classInfo = object.getClass();
            CxRequestMapping declaredAnnotation = classInfo.getDeclaredAnnotation(CxRequestMapping.class);
            String baseUrl = "";
            if (declaredAnnotation != null) {
                // 获取类上的url映射地址
                baseUrl = declaredAnnotation.value();
            }
            // 4.判断方法上是否有加url映射地址
            Method[] declaredMethods = classInfo.getDeclaredMethods();
            for (Method method : declaredMethods) {
                // 判断方法上是否有加url映射注解
                CxRequestMapping methodExtRequestMapping = method.getDeclaredAnnotation(CxRequestMapping.class);
                if (methodExtRequestMapping != null) {
                    String methodUrl = baseUrl + methodExtRequestMapping.value();
                    // springmvc 容器对象 keya:请求地址 ,vlue类
                    urlBeans.put(methodUrl, object);
                    // springmvc 容器对象 key:请求地址 ,value 方法名称
                    urlMethods.put(methodUrl,method);


                }
            }
        }

    }
}
