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

var nickname = null;

var ski_resorts = [
    ["Big Sky Resort", 45.28, -111.4, 1],
    ["Sundance Mountain Resort", 40.393329, -111.588772, 2],
    ["Badger Pass Ski Area", 37.662217, -119.663344, 3],
    ["June Mountain Ski Area", 37.767874, -119.090704, 4],
    ["Mammoth Mountain", 37.651021, -119.026706, 5],
    ["Ski Santa Fe", 35.796174, -105.802357, 6],
    ["Killington Ski Area", 43.625918, -72.796370, 7],
    ["Plattekill Mountain", 42.290269, -74.653214, 8],
    ["Liberty Mountain Resort", 39.763635, -77.375373, 9],
    ["Whitetail Resort", 39.741752, -77.933335, 10]
];

getData(10, "skiing");
getData(10, "road");

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

function refreshData() {
    var num = parseInt(document.getElementById("mySelect").value);
    getData(num, "skiing");
    getData(num, "road");
}

function getData(num, post) {
    fetch('/data?num=' + num.toString() + '&post=' + post).then(response => response.json()).then((data) => {
    const dataListElement = document.getElementById(post + '-data-container');
    dataListElement.innerHTML = '';
    var i;
    for (i = 0; i < data.length; i++) {
        dataListElement.appendChild(createListElement(data[i].text, data[i].nickname));
    }
    });
}

/** Creates an <li> element containing text. */
function createListElement(text, nickname) {
    const liElement = document.createElement('li');
    liElement.innerText = text + " by " + nickname;
    return liElement;
}

function initMap() {
    var map = new google.maps.Map(
      document.getElementById('map'),
      {center: {lat: 40, lng: -97}, zoom: 4});

    for (var i = 0; i < ski_resorts.length; i++) {
        var ski = ski_resorts[i];
        console.log(ski)
        var marker = new google.maps.Marker({
            position: { lat: ski[1], lng: ski[2] },
            map: map,
            title: ski[0],
            zIndex: ski[3]
        });
    }
}

function getAccount() {
    console.log("getting account");
    fetch('/login').then(response => response.json()).then((data) => {
    var login_button = document.getElementById("login");
    var logout_button = document.getElementById("logout");
    var no_user_div = document.getElementById("no-user");
    var user_div = document.getElementById("user");
    var welcome_header = document.getElementById("welcome");

    nickname = data.nickname;
    
    if (data.loginUrl != null) {
        no_user_div.style.display = "block";
        user_div.style.display = "none";
        login_button.href = data.loginUrl;
    }
    else {
        no_user_div.style.display = "none";
        user_div.style.display = "block";
        if (data.nickname == null) {
            logout_button.style.display = "none";
            welcome_header.innerText = "Set nickname below";
        }
        else {
            logout_button.style.display = "block";
            welcome_header.innerText = "Welcome " + data.nickname + "!";
            logout_button.href = data.logoutUrl;
        }
    }});
}

function getNickname() {
    console.log("determining comments section");
    fetch('/login').then(response => response.json()).then((data) => {
    
    var comments = document.querySelectorAll(".comments");
    var pref_comments = document.getElementById("pref-comments"); 

    if (data.id == null) {
        for (var i = 0; i < comments.length; i++) {
            comments[i].style.display = 'none';
        }
        pref_comments.style.display = 'none';
    }
    else {
        for (var i = 0; i < comments.length; i++) {
            comments[i].style.display = 'block';
        }
        pref_comments.style.display = 'block';
    }});
}
