package jp.co.shcl.capture_camera.model

/**
 * カメラの向き（前面・背面）を示す列挙型クラス
 * @property FRONT  前面
 * @property BACK   背面
 */
enum class CameraFacing {
    FRONT,
    BACK;

    /**
     *  自身と逆向きの[CameraFacing]を取得.
     *  @return 自身が[FRONT]の場合[BACK], [BACK]の場合[FRONT]
     *  */
    fun toggle(): CameraFacing =
        if (this == FRONT) BACK else FRONT
}