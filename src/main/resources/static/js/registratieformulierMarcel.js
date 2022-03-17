document.querySelector('#btnStore').addEventListener('click', ()=>{
    maakCustomer()
})
document.querySelector('#btnPostcodeApi').addEventListener('click', ()=>{
    postcodeApi()
})
document.querySelector('#btnPostcodeRegEx').addEventListener('click', ()=>{
    postcodeRegEx()
})
document.querySelector('#btnEmailCheck').addEventListener('click', ()=>{
    emailChecker()
})
document.querySelector('#btnPasswordCheck').addEventListener('click', ()=>{
    passwordChecker()
})
document.querySelector('#btnDobCheck').addEventListener('click', ()=>{
    leeftijdChecker()
})
document.querySelector('#btnBsnCheck').addEventListener('click', ()=>{
    bsnChecker()
})
document.querySelector('#btnVoortgang').addEventListener('click', ()=>{
    voortgangsMeter()
})
let voortgangsgetal=0
function voortgangsMeter(){
    const extra=42
    voortgangsgetal = voortgangsgetal+ extra  
    console.log(voortgangsgetal)
    document.getElementById('voortgang').style.width=`${voortgangsgetal}px`
}
function bsnChecker(){
    let bsn = document.querySelector('#bsn').value
    document.querySelector('#bsnResult').innerHTML=""
    const MIN_LENGTH = 8;
    const MAX_LENGTH = 9;
    if (bsn.length < MIN_LENGTH || bsn.length > MAX_LENGTH){
        document.querySelector('#bsnResult').innerHTML="bsn voldoet niet aan lengte eis"
    }
    let regex = new RegExp(/^[^a-zA-Z]+$/)
    if (!regex.test(bsn)){
        document.querySelector('#bsnResult').innerHTML="bsn heeft letters"
    }
    if (bsn.length == 8){
        bsn = "0" + bsn; //prepend 0 to ensure bsn consists of 9 numbers
    }
    const FACTORS = [9, 8, 7, 6, 5, 4, 3, 2, -1];
    const DIVISOR = 11
    let sum = 0;
    for (i = 0; i < bsn.length; i++) {
        let digit = bsn.substring(i, i + 1);
        sum += digit * FACTORS[i];
    }
    console.log(sum%DIVISOR);
    if (sum%DIVISOR !== 0){

        document.querySelector('#bsnResult').innerHTML="geen geldig bsn"
    }
    //document.querySelector('#voortgang').style.width=100


}
function leeftijdChecker(){
    let dateOfBirth = document.querySelector('#gebdatum').value
    const d = new Date(dateOfBirth);
    const maanden = Date.now() - d.getTime();
    const newDate= new Date(maanden)
    const year = newDate.getUTCFullYear();
    const age = Math.abs(year - 1970);
    if (age<18){
        document.querySelector('#dobResult').innerHTML="te jong"
    } else{
        document.querySelector('#dobResult').innerHTML=""
    }

}
function passwordChecker(){
    let regex = new RegExp(/^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9])(?!.*\s).{8,64}$/i)
    let passwordLengte= document.querySelector('#password').value.length
    let passwordCheckerTekst=""
    if (!regex.test(document.querySelector('#password').value)&&(passwordLengte>64||passwordLengte<8)){
        passwordCheckerTekst="password te lang64/kort8 en 1 Kleine, 1 Hoofdletter 1 getal 1 special karakter"
    }
    else if(passwordLengte>64||passwordLengte<8){
        passwordCheckerTekst="password te lang64/kort8"
    }else if(!regex.test(document.querySelector('#password').value)){
        passwordCheckerTekst="1 Kleine, 1 Hoofdletter 1 getal 1 special karakter"
    }
    else{
        passwordCheckerTekst="na aanmelden wordt wachtwoord getest op gehackt en voorkomen bij onze klanten"
    }
    document.querySelector('#passwordResult').innerHTML =passwordCheckerTekst
}
function emailChecker() {
    let regex = new RegExp(/^[a-zA-Z0-9_+&*-]+(?:\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,7}$/i)
    let emailLengte= document.querySelector('#email').value.length
    let emailCheckerTekst=""
    if (!regex.test(document.querySelector('#email').value)&&emailLengte>30){
        emailCheckerTekst="email niet correct EN email te lang"
    }
    else if(emailLengte>30){
        emailCheckerTekst="email te lang"
    }else if(!regex.test(document.querySelector('#email').value)){
        emailCheckerTekst="email niet correct"
    }
    document.querySelector('#emailResult').innerHTML =emailCheckerTekst

}
function postcodeRegEx() {
    let regex = new RegExp(/^[1-9][0-9]{3}[\s]?[A-Za-z]{2}$/i);
    let postcode = document.querySelector('#postcode').value
    //console.log('pc is valide: ' + regex.test(postcode))
    if (regex.test(postcode)){
        document.querySelector('#postcodeResult').innerHTML=""
    }else{
        document.querySelector('#postcodeResult').innerHTML="postcode verkeerd"
    }
}
function maakCustomer() {
    const em =  document.querySelector('#email').value
    const pw =  document.querySelector('#password').value
    const vn =  document.querySelector('#voornaam').value
    const vv =  document.querySelector('#voorvoegsel').value
    const an =  document.querySelector('#achternaam').value
    const gb =  document.querySelector('#gebdatum').value
    const bsn =  document.querySelector('#bsn').value
    const tel =  document.querySelector('#telefoon').value
    const sn =  document.querySelector('#straatnaam').value
    const hn =  document.querySelector('#huisnummer').value
    const tvv =  document.querySelector('#toevoegsel').value
    const pc =  document.querySelector('#postcode').value
    const pn =  document.querySelector('#stad').value
    let data = {
        "idAccount": 1,
        "email": em,"password": pw,"firstName": vn,"namePrefix": vv,"lastName": an,
        "dob": gb,"bsn": bsn,"telephone": tel,
        "address": {"idAddress": 1,"streetName": sn,"houseNo": hn,"houseAdd": tvv,
            "postalCode": pc,"city": pn
        }
    };

    fetch('http://localhost:8080/users/register', {
        method: 'POST',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)  // moet worden omgezet naar een string
    })
        .then(response => {

                console.log(response)
                //const te = response.body
                //document.querySelector('#bericht').innerHTML = te.roman;
                return response.json()


            }
        )
        .then(data => {

            document.querySelector('#divResult').innerHTML = data.email;
        })
    // .catch((error) => {
    //     console.error('Foutje', error);
    // })
    ;
}

function postcodeApi(){

    let postcode = document.querySelector('#postcode').value
    //let postcode = "2513ST"
    let huisnummer = document.querySelector('#huisnummer').value
    //let huisnummer = 178
    // als postcode een valide postcode is nummer niet leeg, dan
    // console.log('pc is valide: ' + regex.test(postcode))


    let formData = `postcode=${postcode}&number=${huisnummer}` //postcode=1234AB&nr=15

    fetch("https://postcode.tech/api/v1/postcode?" + formData , {
        headers: {
            'Authorization': 'Bearer 5cc336e3-c924-44d6-a44f-d8b9e5e0ddb9',
        },
    })
        .then(response => {

                console.log(response)
                //const te = response.body
                //document.querySelector('#bericht').innerHTML = te.roman;
                return response.json()


            }
        )
        //.then(json => console.log(json.message))
        .then(json => {
            console.log(json.street)
            document.querySelector('#straatnaam').value=(json.street)
            document.querySelector('#stad').value=json.city
        })
}
