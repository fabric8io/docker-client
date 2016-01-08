/**
* Copyright (C) 2011 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*         http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
 */
package schemagen
import (
	"unicode"
	"strings"
	"bytes"
)

func capitalizeFirst(s string) string {
	a := []rune(s)
	a[0] = unicode.ToUpper(a[0])
	return string(a)
}

func capitalizeClassName(s string) string {
	a := strings.Split(s, ".")
	size := len(a)

	if (size == 1) {
		return capitalizeFirst(a[0])
	}

	var buffer bytes.Buffer
	for i := 0; i < size - 1; i++ {
		buffer.WriteString(a[i])
		buffer.WriteString(".")
	}
	buffer.WriteString(capitalizeFirst(a[size - 1]))
	return buffer.String()
}
