/*
 * Copyright 2013 Tsukasa Kitachi
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

import java.nio.file.{Path, Paths}

trait PathSupport {

  /**
   * AtPath for Path
   */
  implicit val pathAtPath: AtPath[Path] = AtPath.by(Paths.get(_: String))

  /**
   * AtPath for List[Path]
   */
  implicit val pathListAtPath: AtPath[List[Path]] = AtPath.listBy(Paths.get(_: String))

}
