/*
 * Copyright 2013-2015 Tsukasa Kitachi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.kxbmap.configs
package support.std

import java.io.File

trait FileSupport {

  /**
   * AtPath for File
   */
  implicit val fileAtPath: AtPath[File] = AtPath.by(new File(_: String))

  /**
   * AtPath for List[File]
   */
  implicit val fileListAtPath: AtPath[List[File]] = AtPath.listBy(new File(_: String))

}
