import { logout } from "./features/login/loginSlice";
import { useAppDispatch } from "./redux/hooks";

const BACKEND_URL = process.env.REACT_APP_BACKEND_URL;
// const BACKEND_URL = "http://localhost:8084";  // for local testing


function Logout() {
    const dispatch = useAppDispatch(); // redux

    (async () => {
        await fetch(`${BACKEND_URL}/logout`, { credentials: "include" }); // call logout at backend
        window.location.replace(`/login`); // redirect to login page

        dispatch(logout()); // update Redux
    })();

    return null;
}

export default Logout;