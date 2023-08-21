import { logout } from "./features/login/loginSlice";
import { useAppDispatch } from "./redux/hooks";

const BACKEND_URL = process.env.REACT_APP_BACKEND_URL;

function Logout() {
    const dispatch = useAppDispatch(); // redux

    // window.location.replace(`${BACKEND_URL}/logout`); // call logout at backend
    window.location.replace(`http://localhost:8084/logout`); // call logout at backend
    window.location.replace(`/login`); // redirect to login page

    dispatch(logout()); // update Redux

    alert("You've logged out");

    return null;
}

export default Logout;