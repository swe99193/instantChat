/**
 * see Redux Typescript tutorials for setup details
 * https://redux.js.org/tutorials/typescript-quick-start
 */

import { configureStore, ThunkAction, Action } from '@reduxjs/toolkit';
import loginReducer from '../features/login/loginSlice';

export const store = configureStore({
    reducer: {
        login: loginReducer,
    },
});

export type AppDispatch = typeof store.dispatch;
export type RootState = ReturnType<typeof store.getState>;
export type AppThunk<ReturnType = void> = ThunkAction<
    ReturnType,
    RootState,
    unknown,
    Action<string>
>;
