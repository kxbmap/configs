package com.github.kxbmap.configs
package ext.std

import java.io.File
import java.nio.file.{Paths, Path}

trait JavaIOConfigs {

  /**
   * AtPath for File
   */
  implicit val fileAtPath: AtPath[File] = AtPath mapBy { new File(_: String) }

  /**
   * AtPath for List[File]
   */
  implicit val fileListAtPath: AtPath[List[File]] = AtPath mapListBy { new File(_: String) }

  /**
   * AtPath for Path
   */
  implicit val pathAtPath: AtPath[Path] = AtPath mapBy { Paths.get(_: String) }

  /**
   * AtPath for List[Path]
   */
  implicit val pathListAtPath: AtPath[List[Path]] = AtPath mapListBy { Paths.get(_: String) }

}
