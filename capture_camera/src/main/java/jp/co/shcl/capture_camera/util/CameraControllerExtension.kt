package jp.co.shcl.capture_camera.util

import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import jp.co.shcl.capture_camera.model.CameraFacing

/**
 * カメラ数を取得.
 * デフォルト前面カメラとデフォルト背面カメラがカウント対象.
 * @return デフォルト前面カメラとデフォルト背面の合計数
 */
internal fun CameraController.cameraCount(): Int {
    return (if (hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) 1 else 0) +
            (if (hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) 1 else 0)
}

/**
 * 現在のカメラ向きを取得.
 * @return カメラ向きを示す[CameraFacing]
 */
internal fun CameraController.currentCameraFacing(): CameraFacing {
    return if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraFacing.BACK else CameraFacing.FRONT
}

/**
 * カメラ向きを設定.
 * @param facing カメラ向きを示す[CameraFacing]
 */
internal fun CameraController.setCameraFacing(facing: CameraFacing) {
    cameraSelector = when (facing) {
        CameraFacing.FRONT -> {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }
        CameraFacing.BACK -> {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
    }
}