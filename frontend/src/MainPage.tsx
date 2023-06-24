import { FormEventHandler } from "react";
import { Form, useNavigate } from "react-router-dom";

import "./MainPage.css";

interface props{
    receiver: string
}

function MainPage() {
    const navigate = useNavigate();
  
    const handleSubmit= (event: React.SyntheticEvent) => {
        event.preventDefault();

        // cast to HTMLInputElement: https://stackoverflow.com/questions/12989741/the-property-value-does-not-exist-on-value-of-type-htmlelement
        let receiver = (document.getElementById("receiver")  as HTMLInputElement).value;
        navigate('/chat',{ state: {receiver} });    // pass variable in "state"
    }

    return(
        
        <div className="container">

            <div className="wrapper">
                <div className="title"><span>Welcome to InstantChat!</span></div>

                <form onSubmit={handleSubmit} method="post">
                    <div className="row">
                        <i className="fa fa-comment faa-float animated"></i>
                        <input type="text" placeholder="Chat with someone..." id="receiver" name="receiver" required/>
                    </div>
                    <div className="row button">
                        <input type="submit" value="Start Chat"/>
                    </div>
                </form>
            </div>
        </div>
    );
}
  
export default MainPage;
  