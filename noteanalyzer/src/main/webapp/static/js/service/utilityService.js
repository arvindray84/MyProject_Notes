'use strict';


noteApp.factory('UtilityService', ['$http', 'toastr', '$q', '$rootScope', '$uibModal', 'WaitingDialog', '$window', function($http, toastr, $q, $rootScope, $uibModal, WaitingDialog, $window) {
	var factory = {
		calculateRate : calculateRate,
		getNPER : getNPER,
		getPV : getPV,
		round : round,
		getParameterByName : getParameterByName,
		getNumberOfMonth : getNumberOfMonth,
		setActiveHeader : setActiveHeader
	};

	return factory;

	function getNumberOfMonth(startDate, endDate) {
		if (Object.prototype.toString.call(startDate) === "[object Date]" && Object.prototype.toString.call(endDate) === "[object Date]") {
			// it is a date
			if (isNaN(endDate.getTime()) || isNaN(startDate.getTime())) { // d.valueOf() could also work
				// date is not valid
				return '';
			} else {
				return getMonthsBetween(startDate,endDate,false);
			}
		}
		return '';

	}
	
	function getMonthsBetween(date1,date2,roundUpFractionalMonths)
	{
	    //Months will be calculated between start and end dates.
	    //Make sure start date is less than end date.
	    //But remember if the difference should be negative.
	    var startDate=date1;
	    var endDate=date2;
	    var inverse=false;
	    if(date1>date2)
	    {
	        startDate=date2;
	        endDate=date1;
	        inverse=true;
	    }

	    //Calculate the differences between the start and end dates
	    var yearsDifference=endDate.getFullYear()-startDate.getFullYear();
	    var monthsDifference=endDate.getMonth()-startDate.getMonth();
	    var daysDifference=endDate.getDate()-startDate.getDate();

	    var monthCorrection=0;
	    //If roundUpFractionalMonths is true, check if an extra month needs to be added from rounding up.
	    //The difference is done by ceiling (round up), e.g. 3 months and 1 day will be 4 months.
	    if(roundUpFractionalMonths===true && daysDifference>0)
	    {
	        monthCorrection=1;
	    }
	    //If the day difference between the 2 months is negative, the last month is not a whole month.
	    else if(roundUpFractionalMonths!==true && daysDifference<0)
	    {
	        monthCorrection=-1;
	    }

	    return (inverse?-1:1)*(yearsDifference*12+monthsDifference+monthCorrection);
	};
	
	function setActiveHeader(id){
		angular.element(document.querySelectorAll(".nav li")).removeClass("active");
		if(angular.element( document.querySelector('#'+id))){
			angular.element( document.querySelector('#'+id)).addClass("active");	
		}
	 }

	
	function calculateRate (nper, pmt, pv, fv, type, guess) {
	    // Sets default values for missing parameters
	    fv = typeof fv !== 'undefined' ? fv : 0;
	    type = typeof type !== 'undefined' ? type : 0;
	    guess = typeof guess !== 'undefined' ? guess : 0.1;

	    // Sets the limits for possible guesses to any
	    // number between 0% and 100%
	    var lowLimit = 0;
	    var highLimit = 1;

	   // Defines a tolerance of up to +/- 0.00005% of pmt, to accept
	   // the solution as valid.
	   var tolerance = Math.abs(0.00000005 * pmt);

	   // Tries at most 40 times to find a solution within the tolerance.
	   for (var i = 0; i < 100; i++) {
	       // Resets the balance to the original pv.
	       var balance = pv;

	       // Calculates the balance at the end of the loan, based
	       // on loan conditions.
	       for (var j = 0; j < nper; j++ ) {
	           if (type == 0) {
	               // Interests applied before payment
	               balance = balance * (1 + guess) + pmt;
	           } else {
	               // Payments applied before insterests
	               balance = (balance + pmt) * (1 + guess);
	           }
	       }

	       // Returns the guess if balance is within tolerance.  If not, adjusts
	       // the limits and starts with a new guess.
	       if (Math.abs(balance + fv) < tolerance) {
	           return guess;
	       } else if (balance + fv > 0) {
	           // Sets a new highLimit knowing that
	           // the current guess was too big.
	           highLimit = guess;
	       } else  {
	           // Sets a new lowLimit knowing that
	           // the current guess was too small.
	           lowLimit = guess;
	       }

	       // Calculates the new guess.
	       guess = (highLimit + lowLimit) / 2;
	   }

	   // Returns null if no acceptable result was found after 40 tries.
	   return Number.NaN;
	};
	
	function calculateRateOld(term, payment, principal, future, type, guess) {
		guess = (guess === undefined) ? 0.01 : guess;
		future = (future === undefined) ? 0 : future;
		type = (type === undefined) ? 0 : type;



		// Set maximum epsilon for end of iteration
		var epsMax = 1e-6;

		// Set maximum number of iterations
		var iterMax = 100;
		var iter = 0;
		var close = false;
		var rate = guess;

		while (iter < iterMax && !close) {
			var t1 = Math.pow(rate + 1, term);
			var t2 = Math.pow(rate + 1, term - 1);

			var f1 = future + t1 * principal + payment * (t1 - 1) * (rate * type + 1) / rate;
			var f2 = term * t2 * principal - payment * (t1 - 1) * (rate * type + 1) / Math.pow(rate, 2);
			var f3 = term * payment * t2 * (rate * type + 1) / rate + payment * (t1 - 1) * type / rate;

			var newRate = rate - f1 / (f2 + f3);

			if (Math.abs(newRate - rate) < epsMax)
				close = true;
			iter++
			rate = newRate;
		}

		if (!close) return Number.NaN + rate;
		return rate;
	};

	function getNPER(rate, payment, present, future, type) {
		// Initialize type
		var type = (typeof type === 'undefined') ? 0 : type;

		// Initialize future value
		var future = (typeof future === 'undefined') ? 0 : future;

		// Evaluate rate and periods (TODO: replace with secure expression evaluator)
		rate = eval(rate);

		// Return number of periods
		var num = payment * (1 + rate * type) - future * rate;
		var den = (present * rate + payment * (1 + rate * type));
		return Math.log(num / den) / Math.log(1 + rate);
	}

	function getPV(rate, periods, payment, future, type) {
		future = future || 0;
		type = type || 0;

		// Return present value
		if (rate === 0) {
			return -payment * periods - future;
		} else {
			return (((1 - Math.pow(1 + rate, periods)) / rate) * payment * (1 + rate * type) - future) / Math.pow(1 + rate, periods);
		}
	};

	function round(value, decimals) {
		if (value) {
			return Number(Math.round(value + 'e' + decimals) + 'e-' + decimals);
		}
	}

	function getParameterByName(name, url) {
		if (!url)
			url = $window.location.href;
		name = name.replace(/[\[\]]/g, "\\$&");
		var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
			results = regex.exec(url);
		if (!results) return null;
		if (!results[2]) return '';
		return decodeURIComponent(results[2].replace(/\+/g, " "));
	}
}]);