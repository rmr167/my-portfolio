// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

//Current index of slide
var slideIndex = 1;

/* changeSlide(n) - changes the slideIndex by n staying in the range 1 to 18
    and then calls showSlide() to display the new slide. Note that n will 
    either 1 or -1.
*/
function changeSlide(n) {
    if (slideIndex + n > 18) {
        slideIndex = 1;
    }
    else if (slideIndex + n < 1) {
        slideIndex = 18;
    }
    else {
        slideIndex += n;
    }
    showSlide();
}

/* showSlide() - displays the slide corresponding with slideIndex in 
    image-container and removes the previous image.
*/
function showSlide() {
  const imgUrl = '/images/Trip-' + slideIndex + '.jpg';

  const imgElement = document.createElement('img');
  imgElement.src = imgUrl;

  const imageContainer = document.getElementById('image-container');

  // Remove the previous image.
  imageContainer.innerHTML = '';

  // Add the new image
  imageContainer.appendChild(imgElement);
}


