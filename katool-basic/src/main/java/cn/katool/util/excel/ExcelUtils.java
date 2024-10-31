package cn.katool.util.excel;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.merge.OnceAbsoluteMergeStrategy;
import org.apache.commons.collections4.CollectionUtils;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
public class ExcelUtils {
    public ByteArrayOutputStream convert(List<String> heads, List data, String sheetName,
                                         List<List<String>> changeColList, List<HashMap<Integer, Integer>> mergeRowMapList,
                                         List<List<String>> changeRowList, List<HashMap<Integer, Integer>> mergeColMapList) {
        // 通过遍历res来进行单元格合并，使用once合并策略
        List<OnceAbsoluteMergeStrategy> mergeStrategyList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(changeColList)
                && CollectionUtils.isNotEmpty(mergeRowMapList)) {
            List<String>[] changRows = (List<String>[]) changeRowList.toArray();
            HashMap<Integer, Integer>[] mergeRowMaps = (HashMap<Integer, Integer>[]) mergeRowMapList.toArray();
            for (int i = 0; i < changRows.length; i++) {
                List<String> changRow = changRows[i];
                for (String s : changRow) {
                    Integer b = Integer.valueOf(s.split(":")[0]);
                    Integer e = Integer.valueOf(s.split(":")[1]);
                    HashMap<Integer, Integer> mergeMap = mergeRowMaps[i];
                    mergeMap.entrySet().forEach(v -> {
                        for (int t = b; t <= e; t++) {
                            mergeStrategyList.add(new OnceAbsoluteMergeStrategy(v.getKey(), v.getValue(), t, t));
                        }
                    });
                }
            }
        }
        if (CollectionUtils.isNotEmpty(changeRowList)
                && CollectionUtils.isNotEmpty(mergeColMapList)) {
            List<String>[] changCols = (List<String>[]) changeColList.toArray();
            HashMap<Integer, Integer>[] mergeColMaps = (HashMap<Integer, Integer>[]) mergeColMapList.toArray();
            for (int i = 0; i < changCols.length; i++) {
                List<String> changCol = changCols[i];
                for (String s : changCol) {
                    Integer b = Integer.valueOf(s.split(":")[0]);
                    Integer e = Integer.valueOf(s.split(":")[1]);
                    HashMap<Integer, Integer> mergeMap = mergeColMaps[i];
                    mergeMap.entrySet().forEach(v -> {
                        for (int t = b; t <= e; t++) {
                            mergeStrategyList.add(new OnceAbsoluteMergeStrategy(v.getKey(), v.getValue(), t, t));
                        }
                    });
                }
            }
        }
        List<List<String>> header = heads.stream().map(v -> {
            List<String> arrayList = new ArrayList();
            arrayList.add(v);
            return arrayList;
        }).collect(Collectors.toList());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelWriterBuilder write = EasyExcel.write(out);
        mergeStrategyList.forEach(v -> {
            write.registerWriteHandler(v);
        });
        write.head(header).sheet(sheetName).doWrite(data);
        return out;
    }
    public ByteArrayOutputStream convert(Class clazz, List data, String sheetName,
                                         List<List<String>> changeColList, List<HashMap<Integer, Integer>> mergeRowMapList,
                                         List<List<String>> changeRowList, List<HashMap<Integer, Integer>> mergeColMapList)     {
        // 通过遍历res来进行单元格合并，使用once合并策略
        List<OnceAbsoluteMergeStrategy> mergeStrategyList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(changeColList)
                && CollectionUtils.isNotEmpty(mergeRowMapList)) {
            List<String>[] changRows = (List<String>[]) changeRowList.toArray();
            HashMap<Integer, Integer>[] mergeRowMaps = (HashMap<Integer, Integer>[]) mergeRowMapList.toArray();
            for (int i = 0; i < changRows.length; i++) {
                List<String> changRow = changRows[i];
                for (String s : changRow) {
                    Integer b = Integer.valueOf(s.split(":")[0]);
                    Integer e = Integer.valueOf(s.split(":")[1]);
                    HashMap<Integer, Integer> mergeMap = mergeRowMaps[i];
                    mergeMap.entrySet().forEach(v -> {
                        for (int t = b; t <= e; t++) {
                            mergeStrategyList.add(new OnceAbsoluteMergeStrategy(v.getKey(), v.getValue(), t, t));
                        }
                    });
                }
            }
        }
        if (CollectionUtils.isNotEmpty(changeRowList)
                && CollectionUtils.isNotEmpty(mergeColMapList)) {
            List<String>[] changCols = (List<String>[]) changeColList.toArray();
            HashMap<Integer, Integer>[] mergeColMaps = (HashMap<Integer, Integer>[]) mergeColMapList.toArray();
            for (int i = 0; i < changCols.length; i++) {
                List<String> changCol = changCols[i];
                for (String s : changCol) {
                    Integer b = Integer.valueOf(s.split(":")[0]);
                    Integer e = Integer.valueOf(s.split(":")[1]);
                    HashMap<Integer, Integer> mergeMap = mergeColMaps[i];
                    mergeMap.entrySet().forEach(v -> {
                        for (int t = b; t <= e; t++) {
                            mergeStrategyList.add(new OnceAbsoluteMergeStrategy(v.getKey(), v.getValue(), t, t));
                        }
                    });
                }
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelWriterBuilder write = EasyExcel.write(out);
        mergeStrategyList.forEach(v -> {
            write.registerWriteHandler(v);
        });
        write.head(clazz).sheet(sheetName).doWrite(data);
        return out;
    }
    public void convert(List<String> heads, List data, String sheetName, OutputStream out,
                        List<List<String>> changeColList, List<HashMap<Integer, Integer>> mergeRowMapList,
                        List<List<String>> changeRowList, List<HashMap<Integer, Integer>> mergeColMapList) {
        // 通过遍历res来进行单元格合并，使用once合并策略
        List<OnceAbsoluteMergeStrategy> mergeStrategyList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(changeColList)
                && CollectionUtils.isNotEmpty(mergeRowMapList)) {
            List<String>[] changRows = (List<String>[]) changeRowList.toArray();
            HashMap<Integer, Integer>[] mergeRowMaps = (HashMap<Integer, Integer>[]) mergeRowMapList.toArray();
            for (int i = 0; i < changRows.length; i++) {
                List<String> changRow = changRows[i];
                for (String s : changRow) {
                    Integer b = Integer.valueOf(s.split(":")[0]);
                    Integer e = Integer.valueOf(s.split(":")[1]);
                    HashMap<Integer, Integer> mergeMap = mergeRowMaps[i];
                    mergeMap.entrySet().forEach(v -> {
                        for (int t = b; t <= e; t++) {
                            mergeStrategyList.add(new OnceAbsoluteMergeStrategy(v.getKey(), v.getValue(), t, t));
                        }
                    });
                }
            }
        }
        if (CollectionUtils.isNotEmpty(changeRowList)
                && CollectionUtils.isNotEmpty(mergeColMapList)) {
            List<String>[] changCols = (List<String>[]) changeColList.toArray();
            HashMap<Integer, Integer>[] mergeColMaps = (HashMap<Integer, Integer>[]) mergeColMapList.toArray();
            for (int i = 0; i < changCols.length; i++) {
                List<String> changCol = changCols[i];
                for (String s : changCol) {
                    Integer b = Integer.valueOf(s.split(":")[0]);
                    Integer e = Integer.valueOf(s.split(":")[1]);
                    HashMap<Integer, Integer> mergeMap = mergeColMaps[i];
                    mergeMap.entrySet().forEach(v -> {
                        for (int t = b; t <= e; t++) {
                            mergeStrategyList.add(new OnceAbsoluteMergeStrategy(v.getKey(), v.getValue(), t, t));
                        }
                    });
                }
            }
        }
        List<List<String>> header = heads.stream().map(v -> {
            List<String> arrayList = new ArrayList();
            arrayList.add(v);
            return arrayList;
        }).collect(Collectors.toList());
        ExcelWriterBuilder write = EasyExcel.write(out);
        mergeStrategyList.forEach(v -> {
            write.registerWriteHandler(v);
        });
        write.head(header).sheet(sheetName).doWrite(data);
    }
    public void convert(Class clazz, List data, String sheetName, OutputStream out,
                        List<List<String>> changeColList, List<HashMap<Integer, Integer>> mergeRowMapList,
                        List<List<String>> changeRowList, List<HashMap<Integer, Integer>> mergeColMapList) {
        // 通过遍历res来进行单元格合并，使用once合并策略
        List<OnceAbsoluteMergeStrategy> mergeStrategyList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(changeColList)
                && CollectionUtils.isNotEmpty(mergeRowMapList)) {
            List<String>[] changRows = (List<String>[]) changeRowList.toArray();
            HashMap<Integer, Integer>[] mergeRowMaps = (HashMap<Integer, Integer>[]) mergeRowMapList.toArray();
            for (int i = 0; i < changRows.length; i++) {
                List<String> changRow = changRows[i];
                for (String s : changRow) {
                    Integer b = Integer.valueOf(s.split(":")[0]);
                    Integer e = Integer.valueOf(s.split(":")[1]);
                    HashMap<Integer, Integer> mergeMap = mergeRowMaps[i];
                    mergeMap.entrySet().forEach(v -> {
                        for (int t = b; t <= e; t++) {
                            mergeStrategyList.add(new OnceAbsoluteMergeStrategy(v.getKey(), v.getValue(), t, t));
                        }
                    });
                }
            }
        }
        if (CollectionUtils.isNotEmpty(changeRowList)
                && CollectionUtils.isNotEmpty(mergeColMapList)) {
            List<String>[] changCols = (List<String>[]) changeColList.toArray();
            HashMap<Integer, Integer>[] mergeColMaps = (HashMap<Integer, Integer>[]) mergeColMapList.toArray();
            for (int i = 0; i < changCols.length; i++) {
                List<String> changCol = changCols[i];
                for (String s : changCol) {
                    Integer b = Integer.valueOf(s.split(":")[0]);
                    Integer e = Integer.valueOf(s.split(":")[1]);
                    HashMap<Integer, Integer> mergeMap = mergeColMaps[i];
                    mergeMap.entrySet().forEach(v -> {
                        for (int t = b; t <= e; t++) {
                            mergeStrategyList.add(new OnceAbsoluteMergeStrategy(v.getKey(), v.getValue(), t, t));
                        }
                    });
                }
            }
        }
        ExcelWriterBuilder write = EasyExcel.write(out);
        mergeStrategyList.forEach(v -> {
            write.registerWriteHandler(v);
        });
        write.head(clazz).sheet(sheetName).doWrite(data);
    }
}
