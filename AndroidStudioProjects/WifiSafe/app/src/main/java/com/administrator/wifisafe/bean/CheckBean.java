package com.administrator.wifisafe.bean;

/**
 * @author lesences  2018/5/26
 */
public class CheckBean {
    public enum CheckState {
        CHECKING,
        WARN,
        UNSAFE,
        SAFE
    }

    private CheckState checkState = CheckState.CHECKING;
    private int optionIndex;
    private String optionStr;

    public CheckState getCheckState() {
        return checkState;
    }

    public void setCheckState(CheckState checkState) {
        this.checkState = checkState;
    }

    public int getOptionIndex() {
        return optionIndex;
    }

    public void setOptionIndex(int optionIndex) {
        this.optionIndex = optionIndex;
    }

    public String getOptionStr() {
        return optionStr;
    }

    public void setOptionStr(String optionStr) {
        this.optionStr = optionStr;
    }
}
