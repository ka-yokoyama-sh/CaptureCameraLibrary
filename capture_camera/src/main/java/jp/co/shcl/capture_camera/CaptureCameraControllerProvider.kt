package jp.co.shcl.capture_camera

/** [CaptureCameraController]のシングルトンインスタンスを提供するクラス */
class CaptureCameraControllerProvider {

    companion object {
        /** インスタンスホルダー */
        private var instance: CaptureCameraController? = null

        /**
         * [CaptureCameraController]のシングルトンインスタンスを取得.
         * @return [CaptureCameraController]のインスタンス
         */
        fun getInstance(): CaptureCameraController = instance ?: synchronized(
            CaptureCameraControllerImpl()
        ) {
            instance ?: CaptureCameraControllerImpl().also { instance = it}
        }

        /**
         * [CaptureCameraController]の実装クラス[CaptureCameraControllerImpl]として
         * シングルトンインスタンスを取得.
         * @return [CaptureCameraControllerImpl]のインスタンス
         */
        internal fun getInstanceAsImpl(): CaptureCameraControllerImpl =
            instance?.impl() ?: synchronized(CaptureCameraControllerImpl()) {
                instance?.impl() ?: CaptureCameraControllerImpl().also { instance = it}
            }

        /**
         * 実装クラス[CaptureCameraControllerImpl]を取得.
         * @return [CaptureCameraControllerImpl]のインスタンス
         */
        private fun CaptureCameraController.impl(): CaptureCameraControllerImpl =
            this as CaptureCameraControllerImpl
    }
}