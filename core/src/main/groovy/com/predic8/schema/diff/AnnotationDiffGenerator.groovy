/* Copyright 2012 predic8 GmbH, www.predic8.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */

package com.predic8.schema.diff

import com.predic8.soamodel.AbstractDiffGenerator
import com.predic8.soamodel.Difference

class AnnotationDiffGenerator extends AbstractDiffGenerator {

  def labelAnnotation, labelRemoved, labelAdded, labelContentAnnotation

  List<Difference> compare() {
    def diffs = []
    if (a && !b) return [new Difference(description: "${labelAnnotation} ${labelRemoved}.", type: 'annotation', safe: true)]
    if (!a && b) return [new Difference(description: "${labelAnnotation} ${labelAdded}.", type: 'annotation', safe: true)]
    if (a && b && compareContents()) return [new Difference(description: "${labelContentAnnotation}.", type: 'annotation', safe: true)]
    []
  }

  boolean compareContents() {
    if (a.documentations.normalizedContent != b.documentations.normalizedContent) return true
    if (a.appinfos.normalizedContent != b.appinfos.normalizedContent) return true
    false
  }

  public AnnotationDiffGenerator() {
    updateLabels()
  }

  protected def updateLabels() {

    labelContentAnnotation = bundle.getString("com.predic8.schema.diff.labelContentAnnotation")
    labelAnnotation = bundle.getString("com.predic8.schema.diff.labelAnnotation")
    labelRemoved = bundle.getString("com.predic8.schema.diff.labelRemoved")
    labelAdded = bundle.getString("com.predic8.schema.diff.labelAdded")
  }

}

