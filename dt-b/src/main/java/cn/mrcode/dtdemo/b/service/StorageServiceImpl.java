package cn.mrcode.dtdemo.b.service;


import cn.mrcode.dtdemo.b.repo.entity.TStorage;
import cn.mrcode.dtdemo.b.repo.mapper.TStorageMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StorageServiceImpl implements StorageService {

    @Autowired
    private TStorageMapper storageMapper;


    @Transactional
    @Override
    public void decrease(String productId, Integer count) {
        // 扣减库存
        LambdaQueryWrapper<TStorage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TStorage::getProductId, productId);
        TStorage storage = storageMapper.selectOne(queryWrapper);
        if (storage == null) {
            throw new RuntimeException("扣减库存失败，商品不存在");
        }

        storage.setUsed(storage.getUsed() + count);
        storage.setResidue(storage.getResidue() - count);
        storageMapper.updateById(storage);

        // 模拟异常（测试回滚）
        if ("error".equals(productId)) {
            throw new RuntimeException("库存服务异常");
        }
    }
}