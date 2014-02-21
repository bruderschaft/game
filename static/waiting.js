var message = document.getElementById("message");

	window.timer = setInterval(function(){call(message)},500);

	var button_stop = document.getElementById("button_stop");
	button_stop.onclick = function () {
			clearTimeout(timer);
			button_stop.childNodes[1].innerHTML = 'Возобновить';			
			button_stop.onclick = call(message);
			button_stop.addEventListener("click", function  (e) {
				window.timer = setInterval(function(){call(document.getElementById("message")); },500);
			});
		}

	function call (outputElement) {
		var xhr = new XMLHttpRequest();
		xhr.open('GET', 'http://192.168.0.105:8078/message', 'true');
		xhr.addEventListener("load", load, false);
			function load (event) {
				var responseText = event.target.responseText;
				outputElement.innerHTML = responseText;
				if (responseText.indexOf("userId") == 0) {	//пришел Id, все ОК
					clearTimeout(window.timer);
					setTimeout("document.location.href = 'http://192.168.0.105:8078/greeting';", 2000);
					
				} else if (responseText.indexOf("Неправильный") == 0) { //TODO: смотреть по коду ошибки
					clearTimeout(window.timer);
					button_stop.onclick = null;
					message.style.background = 'rgba(245, 11, 0, 0.7)';
					button_stop.childNodes[1].innerHTML = "На главную";
					button_stop.childNodes[1].style.textDecoration = "underline";
					button_stop.childNodes[1].href = "http://192.168.0.105:8078/index";
				};
			}
		xhr.send(null);
		var counter = +(document.getElementById("counter").innerHTML);
		counter++;
		document.getElementById("counter").innerHTML = counter;
	}