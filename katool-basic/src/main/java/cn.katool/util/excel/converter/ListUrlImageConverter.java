package cn.katool.util.excel.converter;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ImageData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.alibaba.excel.util.IoUtils;
import com.alibaba.excel.util.ListUtils;
import org.springframework.util.ObjectUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import static com.alibaba.excel.converters.url.UrlImageConverter.urlConnectTimeout;
import static com.alibaba.excel.converters.url.UrlImageConverter.urlReadTimeout;
//extends UrlImageConverter
public class ListUrlImageConverter implements  Converter<List<URL>>{
    private InputStream convertToInputStream(URL value){
        InputStream inputStream = null;
        try {
            URLConnection urlConnection = value.openConnection();
            urlConnection.setConnectTimeout(urlConnectTimeout);
            urlConnection.setReadTimeout(urlReadTimeout);
            inputStream = urlConnection.getInputStream();
            // 这里不能关闭
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            return inputStream;
        }
    }
    @Override
    public WriteCellData<?> convertToExcelData(List<URL> value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws IOException {
        if (ObjectUtils.isEmpty(value)){
            return new WriteCellData<>("　");
        }
        List<InputStream> collect = value.stream().map(this::convertToInputStream).collect(Collectors.toList());
        ArrayList<ImageData> imageDataList = ListUtils.newArrayList();
        int i=0;
        for (InputStream inputStream : collect) {
            if (inputStream==null){
                continue;
            }
            byte[] bytes = IoUtils.toByteArray(inputStream);
            inputStream.close();
            ImageData e = new ImageData();
            e.setImage(bytes);
            e.setRelativeFirstRowIndex(0);
            e.setRelativeLastRowIndex(0);
            e.setRelativeFirstColumnIndex(i);
            e.setRelativeLastColumnIndex(i);
            i++;
            imageDataList.add(e);
        }
        WriteCellData<Object> res = new WriteCellData<>();
        res.setType(CellDataTypeEnum.STRING);
        res.setImageDataList(imageDataList);
        res.setStringValue("　");
        return res;
    }
    public ArrayList<ImageData> convertValueToExcelData(List<URL> value){
        if (ObjectUtils.isEmpty(value)){
            return null;
        }
        List<InputStream> collect = value.stream().map(this::convertToInputStream).collect(Collectors.toList());
        ArrayList<ImageData> imageDataList = ListUtils.newArrayList();
        int i=0;
        for (InputStream inputStream : collect) {
            if (inputStream==null){
                continue;
            }
            byte[] bytes = new byte[0];
            try {
                bytes = IoUtils.toByteArray(inputStream);
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ImageData e = new ImageData();
            e.setImage(bytes);
            e.setRelativeFirstRowIndex(0);
            e.setRelativeLastRowIndex(0);
            e.setRelativeFirstColumnIndex(i);
            e.setRelativeLastColumnIndex(i);
            i++;
            imageDataList.add(e);
        }
        return imageDataList;
    }
}
