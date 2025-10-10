package dev.go.atlas.entity;


import lombok.Data;

/**
 * 代码生成相关配置
 *
 * @author ruoyi
 */
@Data
public class GenConfig {

    /**
     * 作者
     */
    public String author;

    /**
     * 生成包路径
     */
    public String packageName;

    /**
     * 自动去除表前缀，默认是true
     */
    public Boolean autoRemovePre = true;

    /**
     * 表前缀(类名不会包含表前缀)
     */
    public String tablePrefix;
}
