package cn.netbuffer.spring.boot4.mcp.demo.mcpclient.constant;

import java.util.Set;

public final class CDynasty {

    private CDynasty() {}

    private static final Set<String> VALID_DYNASTIES = Set.of(
            "先秦", "秦", "汉", "西汉", "东汉", "三国", "魏", "蜀", "吴",
            "晋", "西晋", "东晋", "南北朝", "隋", "唐", "五代十国",
            "宋", "北宋", "南宋", "辽", "金", "元", "明", "清",
            "近代", "现代", "当代"
    );

    public static boolean isValid(String dynasty) {
        return dynasty != null && VALID_DYNASTIES.contains(dynasty);
    }

}
