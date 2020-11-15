package com.bookstore.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.bookstore.common.Const;
import com.bookstore.common.ResponseCode;
import com.bookstore.common.ServerResponse;
import com.bookstore.dao.CategoryMapper;
import com.bookstore.dao.ProductMapper;
import com.bookstore.pojo.Category;
import com.bookstore.pojo.Product;
import com.bookstore.service.ICategoryService;
import com.bookstore.service.IProductService;
import com.bookstore.utils.DateTimeUtil;
import com.bookstore.utils.PropertiesUtil;
import com.bookstore.vo.ProductDetailVo;
import com.bookstore.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: Tian
 * @time: 2020/7/21 20:56
 */
@Service("iProductService")
public class IProductServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;


    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ProductDetailVo productDetailVo;

    @Autowired
    private ProductListVo productListVo;
    @Autowired
    private ICategoryService iCategoryService;

    public ServerResponse<String> addOrUpdateProduct(Product product) {
        if (product == null) {
            return ServerResponse.createByErrorMessage("参数不正确");
        } else {
            if (org.apache.commons.lang3.StringUtils.isNotBlank(product.getSubImages())) {
                String[] subImageArray = product.getSubImages().split(",");
                if (subImageArray.length > 0) {
                    product.setMainImage(subImageArray[0]);
                }
            }
            //条件判断不必这莫复杂,可以精简
            if (product.getId() != null) {
                Product productOne = productMapper.selectByPrimaryKey(product.getId());
                if (productOne != null) {
                    int result = productMapper.updateByPrimaryKey(product);
                    if (result > 0) {
                        return ServerResponse.createBySuccessMessage("产品更新成功");
                    }
                    return ServerResponse.createByErrorMessage("更新产品失败");
                } else {
                    int result = productMapper.insert(product);
                    if (result > 0) {
                        return ServerResponse.createBySuccessMessage("增加产品成功");
                    }
                    return ServerResponse.createByErrorMessage("增加产品失败");
                }
            } else {
                return ServerResponse.createByErrorMessage("新增或更新的产品参数不正确");
            }
        }
    }

    public ServerResponse<String> setSaleStatus(Integer productId, Integer status) {
        if (productId == null || status == null) {
            return ServerResponse.createByErrorMessage("参数错误,请重新输入");
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product != null) {
            product.setStatus(status);
            int count = productMapper.updateByPrimaryKey(product);
            if (count > 0) {
                return ServerResponse.createBySuccessMessage("修改产品销售状态成功");
            }
            return ServerResponse.createByErrorMessage("修改产品销售状态失败");
        }
        return ServerResponse.createByErrorMessage("商品id不存在");
    }

    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId) {

        if (productId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createByErrorMessage("产品已下架或者删除");
        }
        //将pojo里的简单对象转化为符合要求的vo对象,再来使用
        // createProductDetailVo(product);
        return ServerResponse.createBySuccess(createProductDetailVo(product));
    }

    //原来的pojo对象不符合我现在的要求,才有了vo对象
    private ProductDetailVo createProductDetailVo(Product product) {

        productDetailVo.setId(product.getId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());
        //一旦PropertiesUtil加载(使用),静态代码快就会执行且只执行一次,就可以将硬盘中的文件以流的形式加载进内存中
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/"));
        //category表的id就是product表的categoryId
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if (category != null) {
            productDetailVo.setParentCategoryId(category.getParentId());
        } else {
            productDetailVo.setParentCategoryId(0);//默认根节点
        }
        //mybatis中拿出来的是毫秒值,我们要对其进行转化
        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        return productDetailVo;
    }

    public ServerResponse<PageInfo> getProductList(Integer pageNum, Integer pageSize) {
        //要想使用mybatis 的page-helper插件,按照以下步骤,有开始,有sql的自动监听拼接,也要有结尾
        //startPage--start开始
        // 填充自己的sql查询逻辑
        //pageHelper-收尾
        //开始
        PageHelper.startPage(pageNum, pageSize);
        //todo 能不能将方法进行优化,这样做性能不高
        List<Product> products = productMapper.selectList();
        //虽然查出来了以上product的list集合,但是我们并不想要,我们想要转换后的简单的集合
        ArrayList<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product product : products) {
            ProductListVo productListVo = createProductListVo(product);
            productListVoList.add(productListVo);
        }
        PageInfo pageResult = new PageInfo(products);
        pageResult.setList(productListVoList);
        return ServerResponse.createBySuccess(pageResult);
    }


    public ServerResponse<PageInfo> searchProduct(Integer productId, String productName, Integer pageNum, Integer pageSize) {
        //这里进行搜索的时候,就算productId和productName为空也无所谓
        PageHelper.startPage(pageNum, pageSize);
        //进行模糊查询
        if (productName != null) {
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }
        List<Product> productList = productMapper.searchProduct(productId, productName);
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product product : productList) {
            ProductListVo productListVo = createProductListVo(product);
            productListVoList.add(productListVo);
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    private ProductListVo createProductListVo(Product product) {
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/"));
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setStatus(product.getStatus());
        return productListVo;
    }

    //和后台查询商品详情很像,但是多了一个产品状态的判断
    public ServerResponse<ProductDetailVo> userGetProductDetail(Integer productId) {

        if (productId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createByErrorMessage("产品已下架或者删除");
        }
        if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()) {
            return ServerResponse.createByErrorMessage("产品已下架或者删除");
        }
        //将pojo里的简单对象转化为符合要求的vo对象,再来使用
        //createProductDetailVo(product);
        return ServerResponse.createBySuccess(createProductDetailVo(product));
    }


    public ServerResponse<PageInfo> getProductByKeywordAndcategoryId(String keyword, Integer categoryId, Integer pageSize,
                                                                     Integer pageNum, String orderby) {
        List<Integer> categoryIdList = Lists.newArrayList();
        if (StringUtils.isBlank(keyword) && categoryId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数错误");
        }
        if (categoryId != null) {
            //这里的categoryId就是category表的id
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if (category == null && StringUtils.isBlank(keyword)) {
                //此时我们不能返回createByErrorMessage,而是返回一个空集合.
                PageHelper.startPage(pageNum, pageSize);
                List list = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo(list);
                return ServerResponse.createBySuccess(pageInfo);
            }
            //此处逻辑存在问题 todo 更正
            categoryIdList = iCategoryService.getDeepCategory(category.getId()).getData();
        }

        //现在处理了两种逻辑了,一种是未传递参数,一种是传递categoryId但是并不存在,且未传递keyword
        //接下来

        if (StringUtils.isNotBlank(keyword)) {
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }
        PageHelper.startPage(pageNum, pageSize);
        //shiyong pagehelper进行辅助排序
        if (StringUtils.isNotBlank(orderby)) {
            if (Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderby)) {
                String[] arr = orderby.split("_");
                //使用PageHelper进行排序,格式  PageHelper.orderBy=("字段名 排序规律")
                PageHelper.orderBy(arr[0] + " " + arr[1]);
            }
        }
        //查询操作
        List<Product> productList = productMapper.getProductByKeywordAndcategoryId(categoryIdList.size() == 0 ? null : categoryIdList, StringUtils.isBlank(keyword) ? null : keyword);

        List<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product product : productList) {
            ProductListVo productListVo = createProductListVo(product);
            productListVoList.add(productListVo);
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }
}



