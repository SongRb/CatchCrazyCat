package cn.edu.sjtu.seiee.songrb.catchcrazycat;

/**
 * Created by Song Rb on 4/27/2017.
 */

class Dot {

    // 对应猫点
    static final int STATUS_ON = 1;
    // 对应空白点
    static final int STATUS_OFF = 0;
    // 对应围墙点
    static final int STATUS_IN = 9;
    private int x, y;
    private int status;

    Dot(int x, int y) {
        super();
        this.x = x;
        this.y = y;
        status = STATUS_OFF;
    }

    int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    int getStatus() {
        return status;
    }

    void setStatus(int status) {
        this.status = status;
    }

    void setXY(int x, int y) {
        this.y = y;
        this.x = x;
    }


}

