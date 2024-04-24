package services;

import enums.Role;
import interfaces.IAdminService;
import models.Branch;
import models.BranchUser;
import models.PaymentMethod;
import stores.BranchStorage;
import stores.BranchUserStorage;
import stores.PaymentMethodStorage;
import utils.StaffUpdateChecker;
import utils.exceptions.TooFewManagersException;
import utils.exceptions.TooManyManagersException;

public class AdminService implements IAdminService{

    @Override
    public BranchUser[] getStaffList(){
        return BranchUserStorage.getAll();
    };

    public Branch[] getBranchList(){
        return BranchStorage.getAll();
    };

    @Override
    public boolean addStaff(BranchUser staff){
        int staffCount = 0;
        int managerCount = 0;
        Branch branch = BranchStorage.get(staff.getBranchID());

        if (staff.getRole() == Role.BRANCHMANAGER){
            for (BranchUser branchUser : BranchUserStorage.getAll()){
                if (branchUser.getBranchID() == staff.getBranchID()){
                    if (branchUser.getRole() == Role.BRANCHMANAGER){
                        managerCount++;
                    }
                    else if (branchUser.getRole() == Role.STAFF){
                        staffCount++;
                    }
                }
            }
            managerCount++;
        } else if (staff.getRole() == Role.STAFF){
            for (BranchUser branchUser : BranchUserStorage.getAll()){
                if (branchUser.getBranchID() == staff.getBranchID()){
                    if (branchUser.getRole() == Role.BRANCHMANAGER){
                        managerCount++;
                    }
                    else if (branchUser.getRole() == Role.STAFF){
                        staffCount++;
                    }
                }
            }
            staffCount++;
        } else {
            throw new IllegalArgumentException("Staff role must be either BRANCHMANAGER or STAFF.");
        }

        try {
            StaffUpdateChecker.check(staffCount, managerCount, branch);
            BranchUserStorage.add(staff);
            return true;
        } catch (TooFewManagersException e) {
            System.out.println("Addition blocked as there will be too many staff in the branch." + e.getMessage());
            System.out.println("Try adding new staff to other branches.");
            return false;
        } catch (TooManyManagersException e) {
            System.out.println("Addition blocked as there will be too many managers in the branch." + e.getMessage());
            System.out.println("Try adding new manager to other branches.");
            return false;
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return false;
        }
    };

    @Override
    public boolean removeStaff(BranchUser staff){
        for (BranchUser branchUser : BranchUserStorage.getAll()){
            if (branchUser.getLoginID() == staff.getLoginID()){
                
                int staffCount = 0;
                int managerCount = 0;


                for (BranchUser curBranchUser : BranchUserStorage.getAll()){
                    if (curBranchUser.getBranchID() == staff.getBranchID()){
                        if (curBranchUser.getRole() == Role.BRANCHMANAGER){
                            managerCount++;
                        }
                        else if (curBranchUser.getRole() == Role.STAFF){
                            staffCount++;
                        }
                    }
                }



                if (Role.BRANCHMANAGER == staff.getRole()){
                    managerCount--;
                }
                else if (Role.STAFF == staff.getRole()){
                    staffCount--;
                }


                try {
                    StaffUpdateChecker.check(staffCount, managerCount, BranchStorage.get(staff.getBranchID()));
                    BranchUserStorage.remove(staff);
                    return true;
                } catch (TooFewManagersException e) {
                    System.out.println("Removal blocked as there will be insufficient managers in the branch." + e.getMessage());
                    return false;
                } catch (TooManyManagersException e) {
                    System.out.println("Removal blocked as there will be insufficient staff in the branch." + e.getMessage());
                    return false;
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                    return false;
                }
            }
        }
        System.out.println("Staff does not exist.");
        return false;
    };

    @Override
    public boolean updateStaff(BranchUser staff){
        try {
            BranchUserStorage.update(staff);
            return true;
        } catch (Exception e) {
            return false;
        }
    };

    @Override
    public boolean promoteStaff(BranchUser staff){

        int staffCount = 0;
        int managerCount = 0;
        Branch branch = BranchStorage.get(staff.getBranchID());

        for (BranchUser branchUser : BranchUserStorage.getAll()){
            if (branchUser.getBranchID() == staff.getBranchID()){
                if (branchUser.getRole() == Role.BRANCHMANAGER){
                    managerCount++;
                }
                else if (branchUser.getRole() == Role.STAFF){
                    staffCount++;
                }
            }
        }

        try {
            StaffUpdateChecker.check(staffCount - 1, managerCount + 1, branch);
            staff.setRole(Role.BRANCHMANAGER);
            return true;
        } catch (TooFewManagersException e) {
            System.out.println("Promotion blocked as there will be insufficient staff in the branch." + e.getMessage());
            return false;
        } catch (TooManyManagersException e) {
            System.out.println("Promotion blocked as there will be too many managers in the branch." + e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return false;
        }
    };

    @Override
    public boolean transferStaff(BranchUser staff, Branch oldBranch, Branch newBranch){


        // currently still at old branch
        int oldStaffCount = 0;
        int oldManagerCount = 0;
        int newStaffCount = 0;
        int newManagerCount = 0;

        for (BranchUser branchUser : BranchUserStorage.getAll()){
            if (branchUser.getBranchID() == oldBranch.getID()){
                if (branchUser.getRole() == Role.BRANCHMANAGER){
                    oldManagerCount++;
                }
                else if (branchUser.getRole() == Role.STAFF){
                    oldStaffCount++;
                }
            }
            else if (branchUser.getBranchID() == newBranch.getID()){
                if (branchUser.getRole() == Role.BRANCHMANAGER){
                    newManagerCount++;
                }
                else if (branchUser.getRole() == Role.STAFF){
                    newStaffCount++;
                }
            }
        }

        if (staff.getRole() == Role.BRANCHMANAGER){
            try {
                StaffUpdateChecker.check(oldStaffCount, oldManagerCount - 1, oldBranch);
            } catch (TooFewManagersException e) {
                System.out.println("Transfer blocked as there will be insufficient managers in the old branch." + e.getMessage());
                return false;
            } catch (TooManyManagersException e) {
                System.out.println("This message should never be seen. ");
                System.out.println("Transfer blocked as there will be too many managers in the old branch." + e.getMessage());
                return false;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                return false;
            }
    
            try {
                StaffUpdateChecker.check(newStaffCount, newManagerCount + 1, newBranch);
            } catch (TooFewManagersException e) {
                System.out.println("This message should never be seen. ");
                System.out.println("Transfer blocked as there will be insufficient managers in the new branch." + e.getMessage());
                return false;
            } catch (TooManyManagersException e) {
                System.out.println("Transfer blocked as there will be too many managers in the new branch." + e.getMessage());
                return false;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                return false;
            }
            staff.setBranchID(newBranch.getID());
            return true;
        }
        else if (staff.getRole() == Role.STAFF){
            try {
                StaffUpdateChecker.check(oldStaffCount - 1, oldManagerCount, oldBranch);
            } catch (TooFewManagersException e) {
                System.out.println("This message should never be seen. ");
                System.out.println("Transfer blocked as there will be too many staff in the old branch." + e.getMessage());
                return false;
            } catch (TooManyManagersException e) {
                System.out.println("Transfer blocked as there will be insufficient staff in the old branch." + e.getMessage());
                return false;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                return false;
            }
    
            try {
                StaffUpdateChecker.check(newStaffCount + 1, newManagerCount, newBranch);
            } catch (TooFewManagersException e) {
                System.out.println("Transfer blocked as there will be too many staff in the new branch." + e.getMessage());
                return false;
            } catch (TooManyManagersException e) {
                System.out.println("This message should never be seen. ");
                System.out.println("Transfer blocked as there will be too many managers in the new branch." + e.getMessage());
                return false;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                return false;
            }
            staff.setBranchID(newBranch.getID());
            return true;
        } else {
            throw new IllegalArgumentException("Staff role must be either BRANCHMANAGER or STAFF.");
        }
    };

    @Override
    public boolean addBranch(Branch branch){
        for (Branch curBranch : BranchStorage.getAll()){
            if (curBranch.getName().equals(branch.getName())){
                System.out.println("Branch already exists. Enter a unique branch name.");
                return false;
            }
        }
        BranchStorage.add(branch);
        return true;
    };

    @Override
    public boolean removeBranch(Branch branch){
        for(Branch curBranch : BranchStorage.getAll()){
            if (curBranch.getID() == branch.getID()){
                BranchStorage.remove(branch);
                return true;
            }
        }
        System.out.println("Branch does not exist.");
        return false;
    };

    @Override
    public boolean addPaymentMethod(PaymentMethod paymentMethod){
        
        for (PaymentMethod curPaymentMethod : PaymentMethodStorage.getAll()){
            if (curPaymentMethod.getPaymentMethod().equals(paymentMethod.getPaymentMethod())){
                System.out.println("Payment method already exists.");
                return false;
            }
        }
        PaymentMethodStorage.add(paymentMethod);
        return true;
    };

    @Override
    public boolean removePaymentMethod(PaymentMethod paymentMethod){
        for (PaymentMethod curPaymentMethod : PaymentMethodStorage.getAll()){
            if (curPaymentMethod.getPaymentMethod().equals(paymentMethod.getPaymentMethod())){
                PaymentMethodStorage.remove(paymentMethod);
                return true;
            }
        }
        System.out.println("Payment method does not exist.");
        return false;
    }

}