package com.github.kxbmap.configs
package support.std

import java.io.File

trait FileSupport {

  /**
   * AtPath for File
   */
  implicit val fileAtPath: AtPath[File] = AtPath by { new File(_: String) }

  /**
   * AtPath for List[File]
   */
  implicit val fileListAtPath: AtPath[List[File]] = AtPath listBy { new File(_: String) }

}
