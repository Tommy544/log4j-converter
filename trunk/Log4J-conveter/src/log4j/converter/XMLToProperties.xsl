<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : XMLToProperties.xsl
    Created on : June 5, 2014, 6:21 PM
    Author     : Lucia Stubnova
    Description: Transforms log4j xml configuration to log4j properties configuration.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:log4j="http://jakarta.apache.org/log4j/" version="1.0">
    <xsl:output method="text"/>
    
    <!--zaklad-->
    <xsl:template match="/">
        <xsl:if test="//log4j:configuration[@debug!='null']">
            <xsl:text>log4j.debug=</xsl:text><xsl:value-of select="//log4j:configuration/@debug"/><xsl:text>&#10;</xsl:text>
        </xsl:if>
        <xsl:if test="//log4j:configuration[@threshold!='null']">
            <xsl:text>log4j.threshold=</xsl:text><xsl:value-of select="//log4j:configuration/@threshold"/><xsl:text>&#10;</xsl:text>
        </xsl:if>
        <xsl:if test="//log4j:configuration[@reset]">
            <xsl:text>log4j.reset=</xsl:text><xsl:value-of select="//log4j:configuration/@reset"/><xsl:text>&#10;</xsl:text>
        </xsl:if>
        <xsl:apply-templates select="//root"/>
        <xsl:apply-templates select="//appender"/>
        <xsl:apply-templates select="//renderer"/>
        <xsl:apply-templates select="//plugin"/>
        <xsl:apply-templates select="//category"/>
        <xsl:apply-templates select="//logger"/>
        <xsl:apply-templates select="//categoryFactory"/>
        <xsl:apply-templates select="//loggerFactory"/>
    </xsl:template>
        
    <!--root-->
    <xsl:template match="//root">
        <xsl:choose>
            <xsl:when test="level">
                <xsl:text>log4j.rootLogger=</xsl:text><xsl:value-of select="translate(level/@value,'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/><xsl:apply-templates select="appender-ref" mode="comma"/>
            </xsl:when>
            <xsl:when test="priority">
                <xsl:text>log4j.rootLogger=</xsl:text><xsl:value-of select="translate(priority/@value,'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/><xsl:apply-templates select="appender-ref" mode="comma"/> 
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="appender-ref">
                    <xsl:text>log4j.rootLogger=</xsl:text><xsl:apply-templates select="appender-ref[1]" mode="noComma"/>
                    <xsl:apply-templates select="appender-ref[position()>1]" mode="comma"/> 
                </xsl:if> 
            </xsl:otherwise>
        </xsl:choose>  
        <xsl:apply-templates select="param"/>  
        <xsl:text>&#10;&#10;</xsl:text> 
    </xsl:template>
    
    <!--root/param-->
    <xsl:template match="//root/param">
        <xsl:text>log4j.rootLogger.</xsl:text><xsl:value-of select="@name"/><xsl:text>=</xsl:text><xsl:value-of select="@value"/><xsl:text>&#10;</xsl:text>    
    </xsl:template>
    
    <!--root/appender-ref s ciarkou-->
    <xsl:template match="//root/appender-ref" mode="comma">
        <xsl:text>, </xsl:text><xsl:value-of select="@ref"/>       
    </xsl:template>
    
    <!--root/appender-ref bez ciarky-->
    <xsl:template match="//root/appender-ref" mode="noComma">
        <xsl:value-of select="@ref"/>       
    </xsl:template>
        
    <!--appender-->
    <xsl:template match="//appender">
        <xsl:text>log4j.appender.</xsl:text><xsl:value-of select="@name"/>=<xsl:value-of select="@class"/><xsl:text>&#10;</xsl:text>
        <xsl:apply-templates select="errorHandler"/>
        <xsl:apply-templates select="param"/>  
        <xsl:apply-templates select="rollingPolicy"/> 
        <xsl:apply-templates select="triggeringPolicy"/> 
        <xsl:apply-templates select="connectionSource"/> 
        <xsl:apply-templates select="layout"/>  
        <xsl:apply-templates select="filter"/>
        <xsl:apply-templates select="errorHandler"/> 
        <xsl:apply-templates select="appender-ref"/>
        <xsl:text>&#10;</xsl:text> 
    </xsl:template>
    
    <!--appender/errorHandler-->
    <xsl:template match="//appender/errorHandler">
        <xsl:text>log4j.appender.</xsl:text><xsl:value-of select="../@name"/>.errorHandler=<xsl:value-of select="@class"/><xsl:text>&#10;</xsl:text>      
        <xsl:apply-templates select="root-ref"/>
        <xsl:apply-templates select="logger-ref"/>
        <xsl:apply-templates select="appender-ref"/>   
        <xsl:apply-templates select="param"/>
    </xsl:template>
    
    <!--appender/errorhandler/param-->
    <xsl:template match="//appender/errorHandler/param">
        <xsl:text>log4j.appender.</xsl:text><xsl:value-of select="../../@name"/>.errorHandler.<xsl:value-of select="@name"/>=<xsl:value-of select="@value"/><xsl:text>&#10;</xsl:text>       
    </xsl:template>
    
    <!--appender/errorhandler/root-ref-->
    <xsl:template match="//appender/errorHandler/root-ref">
        <xsl:text>log4j.appender.</xsl:text><xsl:value-of select="../../@name"/><xsl:text>.errorHandler.root-ref=true</xsl:text><xsl:text>&#10;</xsl:text>        
    </xsl:template>
    
    <!--appender/errorhandler/logger-ref-->
    <xsl:template match="//appender/errorHandler/logger-ref">
        <xsl:text>log4j.appender.</xsl:text><xsl:value-of select="../../@name"/>.errorHandler.logger-ref=<xsl:value-of select="@ref"/><xsl:text>&#10;</xsl:text>       
    </xsl:template>
    
    <!--appender/errorhandler/appender-ref-->
    <xsl:template match="//appender/errorHandler/appender-ref">
        <xsl:text>log4j.appender.</xsl:text><xsl:value-of select="../../@name"/>.errorHandler.appender-ref=<xsl:value-of select="@ref"/><xsl:text>&#10;</xsl:text>        
    </xsl:template>
    
    <!--appender/param-->
    <xsl:template match="//appender/param">
        <xsl:text>log4j.appender.</xsl:text><xsl:value-of select="../@name"/>.<xsl:value-of select="@name"/>=<xsl:value-of select="@value"/><xsl:text>&#10;</xsl:text>     
    </xsl:template>
    
    <!--appender/rollingPolicy-->
    <xsl:template match="//appender/rollingPolicy">
        <xsl:choose>
            <xsl:when test="../rollingPolicy[@name]">
                <xsl:text>log4j.appender.</xsl:text><xsl:value-of select="../@name"/>.rollingPolicy.<xsl:value-of select="@name"/>=<xsl:value-of select="@class"/><xsl:text>&#10;</xsl:text>  
                <xsl:apply-templates select="param" mode="name"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>log4j.appender.</xsl:text><xsl:value-of select="../@name"/>.rollingPolicy=<xsl:value-of select="@class"/><xsl:text>&#10;</xsl:text>  
                <xsl:apply-templates select="param" mode="noName"/>
            </xsl:otherwise> 
        </xsl:choose>
    </xsl:template>
    
    <!--appender/rollingPolicy/param mode:name-->
    <xsl:template match="//appender/rollingPolicy/param" mode="name">
        <xsl:text>log4j.appender.</xsl:text><xsl:value-of select="../../@name"/>.rollingPolicy.<xsl:value-of select="../@name"/>.<xsl:value-of select="@name"/>=<xsl:value-of select="@value"/><xsl:text>&#10;</xsl:text>     
    </xsl:template>
    
    <!--appender/rollingPolicy/param mode:noName-->
    <xsl:template match="//appender/rollingPolicy/param" mode="noName">
        <xsl:text>log4j.appender.</xsl:text><xsl:value-of select="../../@name"/>.rollingPolicy.<xsl:value-of select="@name"/>=<xsl:value-of select="@value"/><xsl:text>&#10;</xsl:text>     
    </xsl:template>
    
    <!--appender/triggeringPolicy-->
    <xsl:template match="//appender/triggeringPolicy">
        <xsl:choose>
            <xsl:when test="../triggeringPolicy[@name]">
                <xsl:text>log4j.appender.</xsl:text><xsl:value-of select="../@name"/>.triggeringPolicy.<xsl:value-of select="@name"/>=<xsl:value-of select="@class"/><xsl:text>&#10;</xsl:text>      
               <xsl:apply-templates select="param" mode="name"/> 
               <xsl:apply-templates select="filter" mode="name"/>   
            </xsl:when> 
            <xsl:otherwise>
                <xsl:text>log4j.appender.</xsl:text><xsl:value-of select="../@name"/>.triggeringPolicy=<xsl:value-of select="@class"/><xsl:text>&#10;</xsl:text>      
                <xsl:apply-templates select="param" mode="noName"/> 
                <xsl:apply-templates select="filter" mode="noName"/>                  
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!--appender/triggeringPolicy/param mode:noName-->
    <xsl:template match="//appender/triggeringPolicy/param" mode="noName">
        <xsl:text>log4j.appender.</xsl:text><xsl:value-of select="../../@name"/>.triggeringPolicy.<xsl:value-of select="@name"/>=<xsl:value-of select="@value"/><xsl:text>&#10;</xsl:text>     
    </xsl:template>
    
    <!--appender/triggeringPolicy/param mode:name-->
    <xsl:template match="//appender/triggeringPolicy/param" mode="name">
        <xsl:text>log4j.appender.</xsl:text><xsl:value-of select="../../@name"/>.triggeringPolicy.<xsl:value-of select="../@name"/>.<xsl:value-of select="@name"/>=<xsl:value-of select="@value"/><xsl:text>&#10;</xsl:text>     
    </xsl:template>
    
    <!--appender/triggeringPolicy/filter mode:noName-->
    <xsl:template match="//appender/triggeringPolicy/filter" mode="noName">
        <xsl:text>log4j.appender.</xsl:text><xsl:value-of select="../../@name"/>.triggeringPolicy.filter.ID=<xsl:value-of select="@class"/><xsl:text>&#10;</xsl:text>     
        <xsl:apply-templates select="param" mode="noName"/>  
    </xsl:template>
    
    <!--appender/triggeringPolicy/filter mode:name-->
    <xsl:template match="//appender/triggeringPolicy/filter" mode="name">
        <xsl:text>log4j.appender.</xsl:text><xsl:value-of select="../../@name"/>.triggeringPolicy.<xsl:value-of select="../@name"/>.filter.ID=<xsl:value-of select="@class"/><xsl:text>&#10;</xsl:text>     
        <xsl:apply-templates select="param" mode="name"/>  
    </xsl:template>
    
    <!--appender/triggeringPolicy/filter/param mode:noName-->
    <xsl:template match="//appender/triggeringPolicy/filter/param" mode="noName">
        <xsl:text>log4j.appender.</xsl:text><xsl:value-of select="../../../@name"/>.triggeringPolicy.filter.ID.<xsl:value-of select="@name"/>=<xsl:value-of select="@value"/><xsl:text>&#10;</xsl:text>     
    </xsl:template>-->
    
    <!--appender/triggeringPolicy/filter/param mode:noName-->
    <xsl:template match="//appender/triggeringPolicy/filter/param" mode="name">
        <xsl:text>log4j.appender.</xsl:text><xsl:value-of select="../../../@name"/>.triggeringPolicy.<xsl:value-of select="../../@name"/>.filter.ID.<xsl:value-of select="@name"/>=<xsl:value-of select="@value"/><xsl:text>&#10;</xsl:text>     
    </xsl:template>-->
    
    <!--appender/connectionSource-->
    <xsl:template match="//appender/connectionSource">
        <xsl:text>log4j.appender.</xsl:text><xsl:value-of select="../@name"/>.connectionSource=<xsl:value-of select="@class"/><xsl:text>&#10;</xsl:text>  
        <xsl:apply-templates select="dataSource"/>   
        <xsl:apply-templates select="param"/> 
    </xsl:template>
    
    <!--appender/connectionSource/param-->
    <xsl:template match="//appender/connectionSource/param">
        <xsl:text>log4j.appender.</xsl:text><xsl:value-of select="../../@name"/>.connectionSource.<xsl:value-of select="@name"/>=<xsl:value-of select="@value"/><xsl:text>&#10;</xsl:text>     
    </xsl:template>
    
    <!--appender/connectionSource/dataSource-->
    <xsl:template match="//appender/connectionSource/dataSource">
        <xsl:text>log4j.appender.</xsl:text><xsl:value-of select="../../@name"/>.connectionSource.DataSource=<xsl:value-of select="@class"/><xsl:text>&#10;</xsl:text>  
        <xsl:apply-templates select="param"/> 
    </xsl:template>
    
    <!--appender/connectionSource/dataSource/param-->
    <xsl:template match="//appender/connectionSource/dataSource/param">
        <xsl:text>log4j.appender.</xsl:text><xsl:value-of select="../../../@name"/>.connectionSource.DataSource.<xsl:value-of select="@name"/>=<xsl:value-of select="@value"/><xsl:text>&#10;</xsl:text>     
    </xsl:template>
    
    <!--appender/layout-->
    <xsl:template match="//appender/layout">
        <xsl:text>log4j.appender.</xsl:text><xsl:value-of select="../@name"/>.layout=<xsl:value-of select="@class"/><xsl:text>&#10;</xsl:text>           
        <xsl:apply-templates select="param"/>  
    </xsl:template>
    
    <!--appender/layout/param-->
    <xsl:template match="//appender/layout/param">
        <xsl:text>log4j.appender.</xsl:text><xsl:value-of select="../../@name"/>.layout.<xsl:value-of select="@name"/>=<xsl:value-of select="@value"/><xsl:text>&#10;</xsl:text>      
    </xsl:template>
        
    <!--appender/filter-->
    <xsl:template match="//appender/filter">
        <xsl:text>log4j.appender.</xsl:text><xsl:value-of select="../@name"/>.filter.ID=<xsl:value-of select="@class"/><xsl:text>&#10;</xsl:text>     
        <xsl:apply-templates select="param"/>  
    </xsl:template>
    
    <!--appender/filter/param-->
    <xsl:template match="//appender/filter/param">
        <xsl:text>log4j.appender.</xsl:text><xsl:value-of select="../../@name"/>.filter.ID.<xsl:value-of select="@name"/>=<xsl:value-of select="@value"/><xsl:text>&#10;</xsl:text>      
    </xsl:template>  
    
    <!--appender/appender-ref-->
    <xsl:template match="//appender/appender-ref">
        <xsl:text>log4j.appender.</xsl:text><xsl:value-of select="../@name"/>.appender-ref=<xsl:value-of select="@ref"/><xsl:text>&#10;</xsl:text>     
    </xsl:template> 
  
    <!--renderer-->
    <xsl:template match="//renderer">
        <xsl:text>log4j.renderer.</xsl:text><xsl:value-of select="@renderedClass"/>=<xsl:value-of select="@renderingClass"/><xsl:text>&#10;</xsl:text>
        <xsl:text>&#10;</xsl:text>
    </xsl:template>
    
    <!--plugin-->
    <xsl:template match="//plugin">
        <xsl:text>log4j.plugin.</xsl:text><xsl:value-of select="@name"/>=<xsl:value-of select="@class"/><xsl:text>&#10;</xsl:text>
        <xsl:apply-templates select="connectionSource"/>
        <xsl:apply-templates select="param"/>
        <xsl:text>&#10;</xsl:text>
    </xsl:template>
    
    <!--plugin/connectionSource-->
    <xsl:template match="//plugin/connectionSource">
        <xsl:text>log4j.plugin.</xsl:text><xsl:value-of select="../@name"/>.connectionSource=<xsl:value-of select="@class"/><xsl:text>&#10;</xsl:text>  
        <xsl:apply-templates select="dataSource"/>   
        <xsl:apply-templates select="param"/> 
    </xsl:template>
    
    <!--plugin/connectionSource/param-->
    <xsl:template match="//plugin/connectionSource/param">
        <xsl:text>log4j.plugin.</xsl:text><xsl:value-of select="../../@name"/>.connectionSource.<xsl:value-of select="@name"/>=<xsl:value-of select="@value"/><xsl:text>&#10;</xsl:text>     
    </xsl:template>
    
    <!--plugin/connectionSource/dataSource-->
    <xsl:template match="//plugin/connectionSource/dataSource">
        <xsl:text>log4j.plugin.</xsl:text><xsl:value-of select="../../@name"/>.connectionSource.DataSource=<xsl:value-of select="@class"/><xsl:text>&#10;</xsl:text>  
        <xsl:apply-templates select="param"/> 
    </xsl:template>
    
    <!--plugin/param-->
    <xsl:template match="//plugin/param">
        <xsl:text>log4j.plugin.</xsl:text><xsl:value-of select="../@name"/>.<xsl:value-of select="@name"/>=<xsl:value-of select="@value"/><xsl:text>&#10;</xsl:text>     
    </xsl:template>
    
    <!--categoryFactory-->
    <xsl:template match="//categoryFactory">
        <xsl:text>log4j.CategoryFactory=</xsl:text><xsl:value-of select="@class"/><xsl:text>&#10;</xsl:text>
        <xsl:apply-templates select="param"/>
        <xsl:text>&#10;</xsl:text>
    </xsl:template>
    
    <!--categoryFactory/param-->
    <xsl:template match="//categoryfactory/param">
        <xsl:text>log4j.CategoryFactory.</xsl:text><xsl:value-of select="@name"/>=<xsl:value-of select="@value"/><xsl:text>&#10;</xsl:text>     
    </xsl:template>
    
    <!--loggerFactory-->
    <xsl:template match="//loggerFactory">
        <xsl:text>log4j.LoggerFactory=</xsl:text><xsl:value-of select="@class"/><xsl:text>&#10;</xsl:text>
        <xsl:apply-templates select="param"/>
        <xsl:text>&#10;</xsl:text>
    </xsl:template>
    
    <!--loggerFactory/param-->
    <xsl:template match="//loggerfactory/param">
        <xsl:text>log4j.LoggerFactory.</xsl:text><xsl:value-of select="@name"/>=<xsl:value-of select="@value"/><xsl:text>&#10;</xsl:text>     
    </xsl:template>
    
    <!--logger-->
    <xsl:template match="//logger">
        <xsl:choose>
            <xsl:when test="level">
                <xsl:text>log4j.logger.<xsl:value-of select="@name"/>=</xsl:text><xsl:value-of select="translate(level/@value,'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/><xsl:apply-templates select="appender-ref" mode="comma"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="appender-ref">
                    <xsl:text>log4j.logger.<xsl:value-of select="@name"/>=</xsl:text><xsl:apply-templates select="appender-ref[1]" mode="noComma"/>
                    <xsl:apply-templates select="appender-ref[position()>1]" mode="comma"/>
                </xsl:if> 
            </xsl:otherwise>
        </xsl:choose>
        <xsl:text>&#10;&#10;</xsl:text>   
    </xsl:template>
    
    <!--logger/appender-ref bez ciarky -->
    <xsl:template match="//logger/appender-ref" mode="comma">
        <xsl:value-of select="@ref"/>       
    </xsl:template>
    
    <!--logger/appender-ref s ciarkou-->
    <xsl:template match="//logger/appender-ref" mode="comma">
        <xsl:text>, </xsl:text><xsl:value-of select="@ref"/>       
    </xsl:template>
    
    <!--category-->
    <xsl:template match="//category">
        <xsl:choose>
            <xsl:when test="level">
                <xsl:text>log4j.logger.<xsl:value-of select="@name"/>=</xsl:text><xsl:value-of select="translate(level/@value,'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/><xsl:apply-templates select="appender-ref" mode="comma"/>
            </xsl:when>
            <xsl:when test="priority">
                <xsl:text>log4j.logger.<xsl:value-of select="@name"/>=</xsl:text><xsl:value-of select="translate(priority/@value,'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/><xsl:apply-templates select="appender-ref" mode="comma"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="appender-ref">
                    <xsl:text>log4j.logger.<xsl:value-of select="@name"/>=</xsl:text><xsl:apply-templates select="appender-ref[1]" mode="noComma"/>
                    <xsl:apply-templates select="appender-ref[position()>1]" mode="comma"/>
                </xsl:if> 
            </xsl:otherwise>
        </xsl:choose>  
        <xsl:text>&#10;&#10;</xsl:text>   
    </xsl:template>
    
   <!--category/appender-ref s ciarkou-->
    <xsl:template match="//category/appender-ref" mode="comma">
        <xsl:text>, </xsl:text><xsl:value-of select="@ref"/>       
    </xsl:template>
    
    <!--category/appender-ref bez ciarky-->
    <xsl:template match="//category/appender-ref" mode="noComma">
        <xsl:value-of select="@ref"/>       
    </xsl:template>
  
    <!--category/param-->
    <xsl:template match="//category/param">
        <xsl:text>log4j.logger.</xsl:text><xsl:value-of select="../@name"/>.<xsl:value-of select="@name"/><xsl:text>=</xsl:text><xsl:value-of select="@value"/><xsl:text>&#10;</xsl:text>            
    </xsl:template>
    
</xsl:stylesheet>
