const toggleSidebar = () => {
    if ($(".sidebar").is(":visible")) {
        $(".sidebar").css("display", "none");
        $(".content").css("margin-left", "0%");
    } else {
        $(".sidebar").css("display", "block");
        $(".content").css("margin-left", "20%");
    }
};
// for searching
const search = () => {
    const query = $("#search-input").val();

    if (query == '') {
        $(".search-result").hide();
    } else {
        //    searching
        //sending request to server
        let url = `http://localhost:8080/search/${query}`;

        fetch(url).then((response) => {
            return response.json();
        }).then((data) => {
            let text = `<div class='list-group'>`;
            data.forEach((contact) => {
                text += `<a href='/user/${contact.cId}/contact' class='list-group-item list-group-action'>${contact.name}</a>`;
            });
            text += `</div>`;
            $(".search-result").html(text);
            $(".search-result").show();
        })
    }
}

function createPayment(){
    const amount =$("#amount").val();
    if(amount=="" || amount==null){
        alert("Enter Amount");
        return;
    }
    $.ajax({
        url:'/user/create_payment',
        data:JSON.stringify({amount:amount}),
        type:'POST',
        contentType:'application/json',
        dataType:'json',
        success:function (response){
            // console.log(response)
            if(response.status='created'){
            //     open payment form
                let options={
                    key:'rzp_test_LJPb0aeV8drYkm',
                    amount:response.amount,
                    currency:response.currency,
                    name:"Cloud Contact",
                    description:"Donation",
                    order_id:response.id,
                    handler:function(response){
                        // console.log(response.razorpay_payment_id)
                        // console.log(response.razorpay_order_id)
                        // console.log(response.razorpay_signature)
                        // console.log(response.razorpay_payment_id)
                        // console.log("payment successful")
                        swal("Success", "Congrats you have donated us ", "success");
                    },
                    prefill:{
                        name:"",
                        email:"",
                        contact:""
                    },
                    notes:{
                        address:"Cloud Contact"
                    },
                    theme:{
                        color:"#3399cc",
                    }
                };
              let rzp= new Razorpay(options);
              rzp.on("payment failed",function (response){
                  console.log(response.error.code)
                  swal("Failed", "Payment Failed", "error");

              })
              rzp.open();
            }
        },
        error:function (error){
            console.log(error)
        }
    })
}